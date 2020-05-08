 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.io.IOException;
 
 public class JavaTester {
 
     private String path;
 
     public JavaTester() {
         this.path = "";
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public static void main(String[] args) {
 
         String codigo_filename = args[0];
         String criterio_filename = args[1];
         String sandbox_path = args[2];
         String codigo = "";
         String criterio = "";
 
         try {
             BufferedReader codigo_buffer = new BufferedReader(new FileReader(codigo_filename));
             String codigo_line;
             while((codigo_line = codigo_buffer.readLine()) != null) {
                 codigo += codigo_line;
             }
             codigo_buffer.close();
         } catch(IOException e) {
             System.out.println("SYSTEM_ERROR!:!Falha ao abrir arquivo de código.");
         }
 
         try {
             BufferedReader criterio_buffer = new BufferedReader(new FileReader(criterio_filename));
             String criterio_line;
             while((criterio_line = criterio_buffer.readLine()) != null) {
                 criterio += criterio_line;
             }
             criterio_buffer.close();
         } catch(IOException e) {
             System.out.println("SYSTEM_ERROR!:!Falha ao abrir arquivo de critério.");
         }
 
         try {
             JavaTester javaTester = new JavaTester();
             javaTester.setPath(sandbox_path);
             System.out.println(javaTester.executeTest(codigo,criterio));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public synchronized String executeTest(String codigo, String criterio) throws Exception {
         // Path
         if (!(new File(path).exists())) throw new Exception("SYSTEM_ERROR!:!Diretório de execução não encontrado.");
 
         // ----- Gera o arquivo do aluno ------
         // descobre o nome da classe
         int pos = codigo.indexOf("class ");
         if (pos == -1) return "CODE_ERROR!:!Não há classe definida.";
         pos = pos + "class ".length();
         while (pos < codigo.length() && codigo.charAt(pos) == ' ') pos++;
         int posFim = pos;
         while (posFim < codigo.length() && 
             (Character.isLetterOrDigit(codigo.charAt(posFim)) || codigo.charAt(posFim) == '_')) posFim++;
         if (posFim == pos) return "CODE_ERROR!:!Não há classe definida.";
         String nomeClasse = codigo.substring(pos, posFim);
         // Compila
         File toDelete;
         toDelete = new File (path+nomeClasse + ".java");
         toDelete.delete();
         toDelete = new File (path+nomeClasse + ".class");
         toDelete.delete();
         String resultCompilacao = Compilar.compilar(path, nomeClasse, codigo);
         if (!resultCompilacao.equals("")) {
             return "CODE_ERROR!:!Falha ao compilar o código.\n"+resultCompilacao;
         }
 
         // Trata o codigo de teste
         criterio = StringConverter.replace(criterio, "\r", "");
 
         // Acrescenta parametro String no AssertEquals que não tiver
         pos = criterio.indexOf("assert");
         while (pos != -1) {
             // Encontra o primeiro parametro
             while (criterio.charAt(pos) != '(') pos++;
             pos++;
             posFim = pos;
             int qtdeParentesesAberto = 0;
             while (qtdeParentesesAberto != 0 || criterio.charAt(posFim) != ',') {
                 if (criterio.charAt(posFim) == '(') qtdeParentesesAberto++;
                 if (criterio.charAt(posFim) == ')') qtdeParentesesAberto--;
                 if (qtdeParentesesAberto > 1000) return "TESTCODE_ERROR!:!Problema no código de teste. Notifique o professor. Não foi possível identificar o primeiro parâmetro. Excesso de parênteses abertos.";
                 if (posFim == criterio.length()-1) return "TESTCODE_ERROR!:!Problema no código de teste. Notifique o professor. Não foi possível identificar o primeiro parâmetro. Fim do arquivo encontrado.";
                 posFim++;
             }
             String parametro = criterio.substring(pos, posFim);
             // Encontrar o segundo parametro 
             qtdeParentesesAberto = 0;
             posFim++;
             while (qtdeParentesesAberto != 0 || (criterio.charAt(posFim) != ',' && criterio.charAt(posFim) != ')')) {
                 if (criterio.charAt(posFim) == '(') qtdeParentesesAberto++;
                 if (criterio.charAt(posFim) == ')') qtdeParentesesAberto--;
                 if (qtdeParentesesAberto > 1000) return "TESTCODE_ERROR!:!roblema no código de teste. Notifique o professor. Não foi possível identificar o último parâmetro. Excesso de parênteses abertos.";
                 if (posFim == criterio.length()-1) return "TESTCODE_ERROR!:!Problema no código de teste. Notifique o professor. Não foi possível identificar o último parâmetro. Fim do arquivo encontrado.";
                 posFim++;
             }
             if (criterio.charAt(posFim) == ')') {
                 // Não tem o ultimo parametro
                 criterio = criterio.substring(0, posFim) + ",\"" + StringConverter.replace(parametro, "\"", "\\\"") + "\"" + criterio.substring(posFim);
             }
             pos = criterio.indexOf("assert", pos); 
         }
 
 
         // Gera o arquivo de teste
         String testClass = "RealizaTesteNaClasseSubmetida";
         toDelete = new File(path+testClass + ".java");
         toDelete.delete();
         toDelete = new File (path+testClass + ".class");
         toDelete.delete();
         generateTestFile(path, testClass+".java", criterio);
         resultCompilacao = Compilar.compilar(path, testClass+".java");
         if (!resultCompilacao.equals("")) {
             if (resultCompilacao.indexOf("cannot find symbol") != -1) {
                 if (resultCompilacao.indexOf("symbol  : class ") != -1) {
                     String nome = extraiPalavraSeguinte(resultCompilacao, "symbol  : class");
                     return "CODE_ERROR!:!Não foi encontrada a classe "+nome+".";
                     } else if (resultCompilacao.indexOf("symbol  : method") != -1) {
                         String nomeMetodo = extraiPalavraSeguinte(resultCompilacao, "symbol  : method");
                         String nomeLocal = extraiPalavraSeguinte(resultCompilacao, "location: class ");
                         return "CODE_ERROR!:!Não foi encontrado o método "+nomeMetodo+" na classe "+nomeLocal+".";
                     }
                     } else if (resultCompilacao.indexOf("cannot be applied to ") != -1) {
                         String mensagem = extraiPalavraSeguinte(resultCompilacao,": ");
                         return "CODE_ERROR!:!Problema com parâmetros: the method "+mensagem;
                         } else if (resultCompilacao.indexOf("incompatible types") != -1) {
                             String found = extraiPalavraSeguinte(resultCompilacao, "found :");
                             String required = extraiPalavraSeguinte(resultCompilacao, "required: ");
                             return "CODE_ERROR!:!Tipos de dados incompatíveis. Foi encontrado "+found+" e era requerido "+required+".";
                         }
                        return "TESTCODE_ERROR!:!Falha ao compilar o código de teste. Entre em contato com o professor.\n"+resultCompilacao;
                     }
 
                     // Executa o teste
                     RuntimeExecutor r = new RuntimeExecutor(3000);
                     String resp = "";
                     if (path.startsWith("/")) { // Linux
                         resp = r.execute("java -cp "+path+" -Djava.security.manager -Dfile.encoding=utf-8 "+testClass, null);
                 } else { // Windows 
                     resp = r.execute("java -cp \""+path+";\" -Djava.security.manager -Dfile.encoding=utf-8 "+testClass, null); // Para teste stand alone o ; foi necessário
             }
             toDelete = new File (path+nomeClasse + ".java");
             toDelete.delete();
             toDelete = new File (path+nomeClasse + ".class");
             toDelete.delete();
             toDelete = new File (path+"logCompilacao.txt");
             toDelete.delete();
             return resp;
         }
 
         private String extraiPalavraSeguinte(String texto, String inicio) {
             int tam = inicio.length();
             if (texto.indexOf(inicio) == -1) return null;
             int posIni = texto.indexOf(inicio) + tam;
             int posFim = texto.indexOf('\n', posIni);
             return texto.substring(posIni, posFim);
         }
 
         private void generateTestFile(String path, String className, String criterio) throws Exception {
             PrintWriter saida = new PrintWriter(new FileWriter(path+className));
             BufferedReader template;
             try {
                 template = new BufferedReader(new FileReader(path+"TestTemplate.java"));
             } catch (Exception ex) {
                 throw new Exception ("SYSTEM_ERROR!:!Arquivo TestTemplate.java não encontrado no diretório " + path);
             }
             String linha;
             while ((linha = template.readLine()) != null) {
                 if (linha.indexOf("/* Insert code here */") != -1) {
                     saida.append(criterio + "\n");
                 } else {
                     saida.append(linha + "\n");
                 }
             }
             saida.close();
         }
 
     }
