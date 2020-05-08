 package managedBeans;
 
 import hibernate.RelatoriosHibernate;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import model.PontosDoDia;
 import model.Usuario;
import util.MinutosEmHoras;
 
 public class RelatoriosBean {
 	
 	private HttpSession session;
 	private List<String> anos;
	private Integer ano;
 	private String mes;
 	private List<PontosDoDia> pontosDoMes;
 	private String horasTrabalhadasMes;
 	private Integer diasTrabalhadosMes;
 	
 	public void pontosDoMesValue() throws ParseException{
 		Usuario u = (Usuario) session.getAttribute("usuario");
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
 		Date dt = df.parse(ano+mes);
 		
 		pontosDoMes = new RelatoriosHibernate().getPontosDoMes(dt, u);
 		
 		int minutosTrabalhados = 0;
 		for (PontosDoDia p : pontosDoMes){
 			minutosTrabalhados += p.getMinutos();
 		}

 		diasTrabalhadosMes = pontosDoMes.size();
 		horasTrabalhadasMes = new MinutosEmHoras().minutosEmHoras(minutosTrabalhados);		
 		}
 	
 	public void getAnosValue(){
 		Usuario u = (Usuario) session.getAttribute("usuario");		
 		anos = new RelatoriosHibernate().getAnos(u);
 		System.out.println(anos);
 	}
 	
 	public RelatoriosBean(){
 		session = new CriaHttpSession().getSession();
 		getAnosValue();
 	}
 
	public Integer getAno() {
 		return ano;
 	}
 
	public void setAno(Integer ano) {
 		this.ano = ano;
 	}
 
 	public String getMes() {
 		return mes;
 	}
 
 	public void setMes(String mes) {
 		this.mes = mes;
 	}
 
 	public List<PontosDoDia> getPontosDoMes() {
 		return pontosDoMes;
 	}
 
 	public void setPontosDoMes(List<PontosDoDia> pontosDoMes) {
 		this.pontosDoMes = pontosDoMes;
 	}
 
 	public String getHorasTrabalhadasMes() {
 		return horasTrabalhadasMes;
 	}
 
 	public void setHorasTrabalhadasMes(String horasTrabalhadasMes) {
 		this.horasTrabalhadasMes = horasTrabalhadasMes;
 	}
 
 	public Integer getDiasTrabalhadosMes() {
 		return diasTrabalhadosMes;
 	}
 
 	public void setDiasTrabalhadosMes(Integer diasTrabalhadosMes) {
 		this.diasTrabalhadosMes = diasTrabalhadosMes;
 	}
 
 	public List<String> getAnos() {
 		return anos;
 	}
 
 	public void setAnos(List<String> anos) {
 		this.anos = anos;
 	}
 }
