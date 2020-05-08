 package com.caaina.client.Widgets.MinhasCaronasWidget;
 
 import java.util.List;
 
 import com.caaina.client.Widgets.CaronaWidget.CaronaWidget;
 import com.caaina.client.Widgets.PerfilWidget.Perfil;
 import com.caaina.client.logica.CaronaAbstrata;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 
 public class MinhasCaronas extends Composite {
 
 	private static MinhasCaronasUiBinder uiBinder = GWT
 			.create(MinhasCaronasUiBinder.class);
 	@UiField VerticalPanel painelCaronas;
 	@UiField Button botaoMinhasCaronas;
 	@UiField Button botaoCaronasComoCaroneiro;
 	@UiField Button botaoCaronasOferecidas;
 	@UiField HorizontalPanel painelBotoesCarona;
 	private Perfil perfil;
 
 	interface MinhasCaronasUiBinder extends UiBinder<Widget, MinhasCaronas> {
 	}
 
 	public MinhasCaronas(Perfil perfil) {
 		initWidget(uiBinder.createAndBindUi(this));
 		setPerfil(perfil);
 		botaoMinhasCaronas.setVisible(false);
 		mostraCaronas("todas");
 	}
 
 	public Perfil getPerfil() {
 		return perfil;
 	}
 
 	public void setPerfil(Perfil perfil) {
 		this.perfil = perfil;
 	}
 	
 	private void mostraCaronas(String contexto){
 		painelCaronas.clear();
 		if(contexto.equals("todas")){
			List<String> caronas = getPerfil().getSistema().getUsuario(getPerfil().getLogin()).getCaronas();
 			criaListaDeCaroans(caronas);
 		}else if(contexto.equals("oferecidas")){
 			List<String> caronas = getPerfil().getSistema().getUsuario(getPerfil().getLogin()).getCaronasOferecidas();
 			criaListaDeCaroans(caronas);
 		}else if(contexto.equals("caroneiro")){
 			List<String> caronas = getPerfil().getSistema().getUsuario(getPerfil().getLogin()).getCaronasComoCaroneiro();
 			criaListaDeCaroans(caronas);
 		}
 	}
 	
 	public void criaListaDeCaroans(List<String> caronas){
 		for(String c : caronas){
 			CaronaAbstrata carona = null;
 			try {
 				carona = getPerfil().getSistema().getCarona(c);
 				CaronaWidget caronaLinha = new CaronaWidget(getPerfil(), carona, "minhas caronas");
 				painelCaronas.add(caronaLinha);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 	}
 
 	
 	
 	@UiHandler("botaoCaronasComoCaroneiro")
 	void onBotaoCaronasComoCaroneiroClick(ClickEvent event) {
 		mostraCaronas("caroneiro");
 		botaoCaronasComoCaroneiro.setVisible(false);
 		botaoCaronasOferecidas.setVisible(true);
 		botaoMinhasCaronas.setVisible(true);
 	}
 	@UiHandler("botaoCaronasOferecidas")
 	void onBotaoCaronasOferecidasClick(ClickEvent event) {
 		mostraCaronas("oferecidas");
 		botaoCaronasComoCaroneiro.setVisible(true);
 		botaoCaronasOferecidas.setVisible(false);
 		botaoMinhasCaronas.setVisible(true);
 	}
 	@UiHandler("botaoMinhasCaronas")
 	void onBotaoMinhasCaronasClick(ClickEvent event) {
 		mostraCaronas("todas");
 		botaoCaronasComoCaroneiro.setVisible(true);
 		botaoCaronasOferecidas.setVisible(true);
 		botaoMinhasCaronas.setVisible(false);
 	}
 }
