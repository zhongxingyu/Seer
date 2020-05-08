 package com.odea;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 
 public class NoAutorizadoPage extends BasePage {
 
 	public NoAutorizadoPage() {
 		
		Label mensaje = new Label("mensaje", "Usted no esta autorizado para ver esta pagina.");
 		BookmarkablePageLink<EntradasPage> linkEntradasPage = new BookmarkablePageLink<EntradasPage>("linkEntradasPage", EntradasPage.class);
 
 		add(mensaje);
 		add(linkEntradasPage);
 	}
 	
 }
