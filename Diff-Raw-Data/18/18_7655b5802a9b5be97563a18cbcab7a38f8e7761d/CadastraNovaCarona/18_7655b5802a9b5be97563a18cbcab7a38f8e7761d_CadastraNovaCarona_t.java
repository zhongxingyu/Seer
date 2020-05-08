 package com.caaina.client.Widgets.CadastraNovaCarona;
 
 import java.util.Date;
 
 import com.caaina.client.Widgets.PerfilWidget.Perfil;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 import com.google.gwt.user.datepicker.client.DatePicker;
 import com.google.gwt.user.datepicker.client.DateBox;
 
 public class CadastraNovaCarona extends Composite {
 
 	private static CadastraNovaCaronaUiBinder uiBinder = GWT
 			.create(CadastraNovaCaronaUiBinder.class);
 	@UiField TextBox campoOrigem;
 	@UiField HorizontalPanel linhaOrigem;
 	@UiField HorizontalPanel linhaDestino;
 	@UiField VerticalPanel linhaData;
 	@UiField TextBox campoHora;
 	@UiField Button botaoCadastrar;
 	@UiField TextBox campoDestino;
 	@UiField VerticalPanel linhaCarona;
 	@UiField VerticalPanel linhaVagas;
 	@UiField TextBox campoVagas;
 	@UiField VerticalPanel campo;
 	@UiField DateBox campoData;
 	private Perfil perfil;
 
 	interface CadastraNovaCaronaUiBinder extends
 			UiBinder<Widget, CadastraNovaCarona> {
 	}
 
 	public CadastraNovaCarona(Perfil perfil) {
 		initWidget(uiBinder.createAndBindUi(this));
 		this.setPerfil(perfil);
 	}
 
 	@UiHandler("campoHora")
 	void onCampoHoraClick(ClickEvent event) {
 		campoHora.setText("");
 	}
 	
 	@UiHandler("botaoCadastrar")
 	void onBotaoCadastrarClick(ClickEvent event) {
 		String origem = campoOrigem.getText();
 		String destino = campoDestino.getText();
 		String hora = campoHora.getText();
 		Date data = campoData.getValue();
 		String vagas = campoVagas.getText();
 		@SuppressWarnings("deprecation")
		String dia = data.getDate()<10? "0"+data.getDate():""+data.getDate();
		String mes = data.getMonth()<10? "0"+(data.getMonth()+1):""+(data.getMonth()+1);
		String dataS = dia+ "/" + mes + "/" + (data.getYear()+1900);
 		try {
 			getPerfil().getSistema().cadastrarCarona(getPerfil().getSessao().getId(), origem, destino, dataS, hora, vagas);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		getPerfil().limpaTela();
 	}
 
 	public Perfil getPerfil() {
 		return perfil;
 	}
 
 	public void setPerfil(Perfil perfil) {
 		this.perfil = perfil;
 	}
 	
 	
 }
