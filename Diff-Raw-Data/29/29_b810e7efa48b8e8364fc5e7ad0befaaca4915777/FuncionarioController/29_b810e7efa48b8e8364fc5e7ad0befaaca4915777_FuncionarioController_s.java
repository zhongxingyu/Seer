 package br.org.aappe.erp.controller;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.validator.Message;
 import br.com.caelum.vraptor.validator.Validations;
 import static br.com.caelum.vraptor.view.Results.*;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import br.org.aappe.erp.annotations.Transactional;
 import br.org.aappe.erp.bean.Funcionario;
 import br.org.aappe.erp.enums.Role;
 import br.org.aappe.erp.enums.Status;
 import br.org.aappe.erp.repository.FuncionarioRepository;
 import br.org.aappe.erp.repository.UnidadeRepository;
 import br.org.aappe.erp.session.EmployeeSession;
 import br.org.aappe.erp.util.Utilities;
 
 /**
  * @author Phelipe Melanias
  */
 @Resource @Path("/admin")
 public class FuncionarioController extends MainController {
     private final FuncionarioRepository repository;
     private final UnidadeRepository unidadeRepository;
 
     public FuncionarioController(Result result, Validator validator, HttpServletResponse response,
                                  EmployeeSession employeeSession, FuncionarioRepository repository, UnidadeRepository unidadeRepository)
     {
         super(result, validator, response, employeeSession);
 
         this.repository = repository;
         this.unidadeRepository = unidadeRepository;
     }
 
     @Get("/funcionario")
     public List<Funcionario> list() {
         result.include("title", "Funcionários");
         return repository.listAllById();
     }
 
     @Get("/funcionario/refresh")
     public List<Funcionario> refresh() {
         return list();
     }
 
     @Get("/funcionario/view/{id}")
     public Funcionario view(int id) {
         result.include("title", "Informações do Funcionário");
         return repository.find(id);
     }
 
     @Get("/funcionario/add")
     public void frmAdd() {
         result.include("title", "Cadastrar Funcionário");
         result.include("roles", Role.getAll());
         result.include("status", Status.getAll());
         result.include("unidades", unidadeRepository.listAllById());
     }
 
     @Transactional
     @Post("/funcionario/add")
     public void add(final Funcionario funcionario) {
         List<Message> errors = validate(funcionario);
 
         validator.addAll(errors);
         validator.onErrorUse(json()).withoutRoot().from(errors).exclude("category").serialize();
 
         //Verificar se algum setor foi selecionado
         if (funcionario.getSetor().getId() == 0)
             funcionario.setSetor(null);
 
         repository.persist(funcionario);
         result.use(json()).withoutRoot().from("OK").serialize();
     }
 
     @Get("/funcionario/edit/{id}")
     public Funcionario frmEdit(int id) {
         result.include("title", "Editar Funcionário");
         result.include("roles", Role.getAll());
         result.include("status", Status.getAll());
         result.include("unidades", unidadeRepository.listAllById());
         return repository.find(id);
     }
 
     @Transactional
     @Post("/funcionario/edit")
     public void edit(final Funcionario funcionario) {
         List<Message> errors = validate(funcionario);
 
         validator.addAll(errors);
         validator.onErrorUse(json()).withoutRoot().from(errors).exclude("category").serialize();
 
         //Verificar se algum setor foi selecionado
         if (funcionario.getSetor().getId() == 0)
             funcionario.setSetor(null);
 
         repository.merge(funcionario);
         result.use(json()).withoutRoot().from("OK").serialize();
     }
 
     @Get("/funcionario/pdf")
     public void pdf() throws IOException {
         try {
             Document document = new Document(PageSize.A3, 18, 18, 18, 18);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
             PdfWriter.getInstance(document, baos);
 
             document.open();
 
             //Tabela
             PdfPTable table = new PdfPTable(7);
             table.setWidthPercentage(100);
 
             table.getDefaultCell().setPaddingTop(4);
             table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
 
             //Célula
             PdfPCell cell;
 
             Font title  = FontFactory.getFont(FontFactory.HELVETICA, 10);
             Font header = FontFactory.getFont(FontFactory.HELVETICA, 8);
             Font normal = FontFactory.getFont(FontFactory.HELVETICA, 7);
 
             //Título da tabela
             cell = new PdfPCell(new Phrase("FUNCIONÁRIOS", title));
             cell.setBackgroundColor(BaseColor.GRAY);
             cell.setHorizontalAlignment(Element.ALIGN_CENTER);
             cell.setPaddingTop(4);
             cell.setColspan(7);
             table.addCell(cell);
 
             List<Funcionario> funcionarios = repository.listAllById();
 
             if (funcionarios.isEmpty()) {
                 cell = new PdfPCell(new Phrase("Nenhum funcionário cadastrado até o momento.", normal));
                 cell.setPadding(6);
                 cell.setColspan(7);
                 table.addCell(cell);
             } else {
                 cell = new PdfPCell(new Phrase("CPF", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 cell = new PdfPCell(new Phrase("NOME", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 cell = new PdfPCell(new Phrase("PERFIL", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 cell = new PdfPCell(new Phrase("ADMISSÃO", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 cell = new PdfPCell(new Phrase("DEMISSÃO", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 cell = new PdfPCell(new Phrase("UNIDADE", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 cell = new PdfPCell(new Phrase("SETOR", header));
                 cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                 cell.setPaddingTop(4);
                 table.addCell(cell);
 
                 for (Funcionario f : funcionarios) {
                     table.addCell(new Phrase(f.getCpf().isEmpty() ? "-" : f.getCpf(), normal));
                     table.addCell(new Phrase(f.getNome(), normal));
                     table.addCell(new Phrase(f.getPerfil().getUserRole(), normal));
 
                     if (f.getAdmissao() != null)
                         table.addCell(new Phrase(f.getAdmissao().toString(), normal));
                     else
                         table.addCell(new Phrase("-", normal));
 
                     if (f.getDemissao() != null)
                         table.addCell(new Phrase(f.getDemissao().toString(), normal));
                     else
                         table.addCell(new Phrase("-", normal));
 
                     if (f.getUnidade() != null)
                         table.addCell(new Phrase(f.getUnidade().getRazaoSocial(), normal));
                     else
                         table.addCell(new Phrase("-", normal));
 
                     if (f.getSetor() != null)
                         table.addCell(new Phrase(f.getSetor().getSigla(), normal));
                     else
                         table.addCell(new Phrase("-", normal));
                 }
             }
 
             document.add(table);
             document.close();
 
             //Salvar PDF
             response.setContentType("application/pdf");
             response.setHeader("Expires", "0");
             response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
             response.setHeader("Pragma", "public");
             response.setHeader("Content-Disposition", "attachment;filename=Funcionarios.pdf");
             response.setContentLength(baos.size());
 
             OutputStream os = response.getOutputStream();
             baos.writeTo(os);
             os.flush();
             os.close();
         } catch (DocumentException e) {
             throw new IOException(e.getMessage());
         }
 
         result.nothing();
     }
 
     private List<Message> validate(final Funcionario funcionario) {
         return new Validations(){{
             //Nome
             if (that(!funcionario.getNome().isEmpty(), "funcionario.nome", "nome"))
                 that(repository.isUniqueName(funcionario), "funcionario.nome", "nome.unico");
 
             //RG ou CPF
             that(funcionario.getRg() != null || !funcionario.getCpf().isEmpty(), "", "rg.ou.cpf");
 
             //RG válido e único
             if (funcionario.getRg() != null && that(funcionario.getRg().toString().length() > 5 && funcionario.getRg().toString().length() < 20, "funcionario.rg", "rg.invalido", 6, 20))
                 that(repository.isUniqueRg(funcionario), "funcionario.rg", "rg.unico");
 
             //CPF válido e único
             if (!funcionario.getCpf().isEmpty() && that(Utilities.cpf(funcionario.getCpf()), "funcionario.cpf", "cpf.invalido"))
                 that(repository.isUniqueCpf(funcionario), "funcionario.cpf", "cpf.unico");
 
             //E-mail
             if (!funcionario.getEmail().isEmpty() && that(Utilities.mail(funcionario.getEmail()), "funcionario.email", "email.invalido"))
                 that(repository.isUniqueMail(funcionario), "funcionario.email", "email.unico");
 
             //Telefone ou Celular
             that(!funcionario.getTelefone().isEmpty() || !funcionario.getCelular().isEmpty(), "", "telefone.ou.celular");
 
             //Unidade
             that(funcionario.getUnidade().getId() > 0, "funcionario.unidade", "unidade_not_selected");
 
            System.out.println("##LOGIN: "+ funcionario.getLogin());
            System.out.println("##SENHA: "+ funcionario.getSenha());
            System.out.println("##CHECK: "+ funcionario.getCheckPass());

             //Login
             if (funcionario.getLogin() != null && that(!funcionario.getLogin().isEmpty(), "login", "setup.login"))
                 that(repository.isUniqueLogin(funcionario), "funcionario.login", "login.unico");
 
             //Senha
             if (funcionario.getId() > 0) {
                 Funcionario old = repository.find(funcionario.getId());
 
                 if (old.getPerfil().equals(Role.REPRESENTANTE) && !funcionario.getPerfil().equals(Role.REPRESENTANTE)) {
                     if (that(!funcionario.getSenha().isEmpty(), "funcionario.senha", "senha") &&
                         that(funcionario.getSenha().length() > 5, "funcionario.senha", "senha.invalida") &&
                         that(funcionario.getSenha().equals(funcionario.getCheckPass()), "", "senha.diferente"))
                        funcionario.setSenha(Utilities.md5(funcionario.getLogin()+funcionario.getSenha()));
                 } else {
                     if ((funcionario.getSenha() != null && funcionario.getCheckPass() != null) &&
                         (!funcionario.getSenha().isEmpty() && that(funcionario.getSenha().length() > 5, "funcionario.senha", "senha.invalida") &&
                          that(funcionario.getSenha().equals(funcionario.getCheckPass()), "", "senha.diferente"))) {
                        funcionario.setSenha(Utilities.md5(funcionario.getLogin()+funcionario.getSenha()));
                     } else {
                         funcionario.setSenha(old.getSenha());
                     }
                 }
             } else {
                 if ((funcionario.getSenha() != null && funcionario.getCheckPass() != null) &&
                     (that(!funcionario.getSenha().isEmpty(), "funcionario.senha", "senha") &&
                      that(funcionario.getSenha().length() > 5, "funcionario.senha", "senha.invalida") &&
                      that(funcionario.getSenha().equals(funcionario.getCheckPass()), "", "senha.diferente")))
                    funcionario.setSenha(Utilities.md5(funcionario.getLogin()+funcionario.getSenha()));
             }
 
             //Data de nascimento
             if (funcionario.getNascimento() != null && funcionario.getAdmissao() != null)
                 that(funcionario.getNascimento().compareTo(funcionario.getAdmissao()) < 0, "funcionario.nascimento", "dataNascimento.maior");
 
             //Data de demissão
             if (funcionario.getAdmissao() != null && funcionario.getDemissao() != null)
                 that(funcionario.getDemissao().compareTo(funcionario.getAdmissao()) > 0, "funcionario.demissao", "dataDemissao.menor");
         }}.getErrors();
     }
 }
