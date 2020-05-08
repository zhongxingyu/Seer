 package logica;
 
 import java.util.List;
 
 public class VisualizadorDePerfil {
 
 	private Usuario usuario;
 	private List<Carona> caronas;
 
 	public VisualizadorDePerfil(Usuario usuario, List<Carona> caronas) {
 		setUsuario(usuario);
 		setCaronas(caronas);
 	}
 
 	private void setCaronas(List<Carona> caronas) {
 		this.caronas = caronas;
 	}
 
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	public void setUsuario(Usuario usuario) {
 		this.usuario = usuario;
 	}
 
 	public String getNome() {
 
 		return usuario.getNome();
 	}
 
 	public String getEndereco() {
 		return usuario.getEndereco();
 	}
 
 	public String getEmail() {
 		return usuario.getEmail();
 	}
 
 	public String getHistoricoDeCaronas() {
 
 		if (usuario.getCaronasOferecidas().size() == 0) {
 			return "";
 		}
 
 		String historicoDeCaronas = "[";
 
 		for (String c : usuario.getCaronasOferecidas()) {
 			historicoDeCaronas += c + ",";
 		}
 
 		if (historicoDeCaronas.length() != 0) {
 			historicoDeCaronas = historicoDeCaronas.substring(0,
 					historicoDeCaronas.length() - 1);
 		}
 		historicoDeCaronas += "]";
 
 		return historicoDeCaronas;
 	}
 
 	public String getHistoricoDeVagasEmCaronas() {
 
 		String saida = "";
 		if (usuario.getCaronasComoCaroneiro().size() == 0) {
 			return "";
 		} else {
 			for (String c : usuario.getCaronasComoCaroneiro()) {
 				saida += c + ",";
 			}
 
 			saida = "[" + saida.substring(0, saida.length() - 1) + "]";
 			return saida;
 		}
 	}
 
 	public String getCaronasSegurasETranquilas() {
 		// TODO Auto-generated method stub
 		return "0";
 	}
 
 	public String getCaronasQueNaoFuncionaram() {
 		// TODO Auto-generated method stub
 		return "0";
 	}
 
 	public String getFaltasEmVagasDeCaronas() {
 
 		int faltas = 0;
 		for (Carona c : caronas) {
 			for (Review r : c.getReviews()) {
				System.out.println(r.getReview() + r.getUsuario());
 
				if (r.getReview().equals("faltou") && r.getUsuario().equals(this.usuario.getLogin())) {
 
 					faltas++;
 				}
 			}
 		}
 
 		return "" + faltas;
 	}
 
 	public String getPresencasEmVagasDeCaronas() {
 		// TODO Auto-generated method stub
 		return "0";
 	}
 
 	public List<String> getAmigos() {
 		return usuario.getAmigos();
 	}
 
 }
