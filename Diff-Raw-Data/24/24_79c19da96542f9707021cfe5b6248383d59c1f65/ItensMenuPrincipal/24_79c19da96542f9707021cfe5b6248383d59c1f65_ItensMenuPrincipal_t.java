 package iu.texto.enums;
 
 public enum ItensMenuPrincipal {
 	STATUS(0, "Atualizar Status"),
 	ATUALIZACOES(1, "Ver o Feed de Notcias"),
	AMIGOS(2, "Lista de Amigos"),
	MENU_INICIAL(3, "Menu Inicial");
 	
 	private int id;
 	private String texto;
 	
 	private ItensMenuPrincipal(int id, String texto){
 		this.id = id;
 		this.texto = texto;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getTexto() {
 		return texto;
 	}
 }
