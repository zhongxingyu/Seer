 package com.sfeir.pocapigmapdmo.client.ui;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.event.MapClickHandler.MapClickEvent;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.event.MarkerDragEndHandler;
 import com.google.gwt.maps.client.event.MarkerDragStartHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.sfeir.pocapigmapdmo.client.CustomMarker;
 import com.sfeir.pocapigmapdmo.client.entities.PoiEntity;
 
 public class PoiViewPanel extends Composite {
 
 	private static final String MODIFIER = "Modifier";
 	private static final String CREER = "Creer";
 	private static PoiViewPanelUiBinder uiBinder = GWT.create(PoiViewPanelUiBinder.class);
 	private MainPanel mainPanel;
 	private MapClickEvent mapCLickEvent;
 	private CustomMarker currentPoi;
 
 	interface PoiViewPanelUiBinder extends UiBinder<Widget, PoiViewPanel> {
 	}
 
 	@UiField
 	TextBox poiName, poiLatitude, poiLongitude;
 	@UiField
 	TextArea poiCommentaire;
 	@UiField
 	Button poiSaveButton, poiDeleteButton;
 
 	private PoiViewPanel() {
 		initWidget(uiBinder.createAndBindUi(this));
 
 		// FIXME BUG TEXTAREA - INTERDICTION DU RESIZE NE MARCHE PAS
 		this.poiCommentaire.getElement().setAttribute("resize", "none");
 	}
 
 	public TextBox getPoiName() {
 		return this.poiName;
 	}
 
 	public void setPoiName(TextBox poiName) {
 		this.poiName = poiName;
 	}
 
 	public TextBox getPoiLatitude() {
 		return this.poiLatitude;
 	}
 
 	public void setPoiLatitude(TextBox poiLatitude) {
 		this.poiLatitude = poiLatitude;
 	}
 
 	public TextBox getPoiLongitude() {
 		return this.poiLongitude;
 	}
 
 	public void setPoiLongitude(TextBox poiLongitude) {
 		this.poiLongitude = poiLongitude;
 	}
 
 	public TextArea getPoiCommentaire() {
 		return this.poiCommentaire;
 	}
 
 	public void setPoiCommentaire(TextArea poiCommentaire) {
 		this.poiCommentaire = poiCommentaire;
 	}
 
 	public Button getSaveButton() {
 		return this.poiSaveButton;
 	}
 
 	public void setSaveButton(Button poiSaveButton) {
 		this.poiSaveButton = poiSaveButton;
 	}
 
 	public CustomMarker getCurrentPoi() {
 		return this.currentPoi;
 	}
 
 	public void setCurrentPoi(CustomMarker currentPoi) {
 		this.currentPoi = currentPoi;
 	}
 
 	public void setCLickEvent(MapClickEvent event) {
 		this.mapCLickEvent = event;
 	}
 
 	public MainPanel getMainPanel() {
 		return this.mainPanel;
 	}
 
 	public void setMainPanel(MainPanel mainPanel) {
 		this.mainPanel = mainPanel;
 	}
 
 	public void setInfos(CustomMarker poi) {
 		setInfos(poi, false);
 	}
 
 	public void setInfos(CustomMarker poi, boolean tempMarker) {
 		this.setCurrentPoi(poi);
 		if (tempMarker) {
 			GWT.log("MAJ des infos du PoiViewPanel pour le poi temporaire");
 			this.poiName.setText(poi.getLibelle());
 			this.poiLatitude.setText(new Double(poi.getLatLng().getLatitude()).toString());
 			this.poiLongitude.setText(new Double(poi.getLatLng().getLongitude()).toString());
 			this.poiCommentaire.setText(poi.getCommentaire());
 		} else {
 			GWT.log("MAJ des infos du PoiViewPanel pour le poi " + poi.toString());
 			this.poiName.setText(poi.getLibelle());
 			this.poiLatitude.setText(new Double(poi.getLatLng().getLatitude()).toString());
 			this.poiLongitude.setText(new Double(poi.getLatLng().getLongitude()).toString());
 			this.poiCommentaire.setText(poi.getCommentaire());
 			this.poiSaveButton.setText(MODIFIER);
 			if (this.mainPanel.getCurrentUser() != null && this.mainPanel.getCurrentUser().isLogged()) {
 				this.poiDeleteButton.setEnabled(true);
 			} else {
 				this.poiDeleteButton.setEnabled(false);
 			}
 		}
 	}
 
 	public void clearInfos() {
 		this.poiName.setText("");
 		this.poiLatitude.setText("");
 		this.poiLongitude.setText("");
 		this.poiCommentaire.setText("");
 		this.poiSaveButton.setText(CREER);
 		this.poiDeleteButton.setEnabled(false);
 	}
 
 	public void setReadonly(boolean readOnly) {
 		this.poiName.setReadOnly(readOnly);
 		this.poiLatitude.setReadOnly(readOnly);
 		this.poiLongitude.setReadOnly(readOnly);
 		this.poiCommentaire.setReadOnly(readOnly);
 		this.poiSaveButton.setEnabled(!readOnly);
 		this.poiDeleteButton.setEnabled(!readOnly && this.poiSaveButton.getText().equals(MODIFIER));
 	}
 
 	@UiHandler("poiDeleteButton")
 	void onPoiDeleteButtonClick(ClickEvent event) {
 
 		// Confirmation avant la suppression
 		final PopupPanel confirmationSuppressionPopup = new PopupPanel(false, true);
 		confirmationSuppressionPopup.setGlassEnabled(true);
 		VerticalPanel dialogContents = new VerticalPanel();
 
 		confirmationSuppressionPopup.setWidget(dialogContents);
 
 		HTML messageHtml = new HTML("<h3>Etes-vous sûr de vouloir supprimer ce point d'intérêt ?</h3>");
 		dialogContents.add(messageHtml);
 		dialogContents.setCellHorizontalAlignment(messageHtml, HasHorizontalAlignment.ALIGN_CENTER);
 
 		Button ouiButton = new Button("Oui", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				confirmationSuppressionPopup.hide();
 
 				// Suppression sur la map
 				getMainPanel().getCartePanel().getGmapPanel().getMap().removeOverlay(getCurrentPoi());
 
 				// Suppression des infos de la PoiView
 				clearInfos();
 
 				// Suppression dans le datastore
 				getMainPanel().getPersistentService().deletePoi(getCurrentPoi().getDatastoreId(),
 						new AsyncCallback<Void>() {
 							final CustomMarker currentPoiLocal = PoiViewPanel.this.getCurrentPoi();
 
 							@Override
 							public void onFailure(Throwable caught) {
 								GWT.log("deletePoi() - PersistentService RPC call failed : " + caught);
 							}
 
 							@Override
 							public void onSuccess(Void result) {
 								GWT.log("deletePoi() - PersistentService RPC call succedded for : "
 										+ this.currentPoiLocal.toString());
 							}
 						});
 
 				// Suppression dans l'objet listePoiDataProvider
 				getMainPanel().getCartePanel().getGmapPanel().getListePoiDataProvider().getList()
 						.remove(getCurrentPoi());
 			}
 		});
 		Button nonButton = new Button("Non", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				// On ne fait rien !
 				confirmationSuppressionPopup.hide();
 			}
 		});
 
 		// Création d'un horizontal panel pour les 2 boutons
 		HorizontalPanel buttonsPanel = new HorizontalPanel();
 
 		ouiButton.setStyleName("btn");
 		nonButton.setStyleName("btn");
 
 		buttonsPanel.add(ouiButton);
 		buttonsPanel.add(nonButton);
 
 		dialogContents.add(buttonsPanel);
 
 		ouiButton.getElement().getParentElement().getStyle().setProperty("textAlign", "right");
 		nonButton.getElement().getParentElement().getStyle().setProperty("textAlign", "left");
 
 		// Annulation du style twitter bootstrap qui affiche des bordures
 		ouiButton.getElement().getParentElement().getStyle().setProperty("borderTop", "0px");
 		nonButton.getElement().getParentElement().getStyle().setProperty("borderTop", "0px");
 		messageHtml.getElement().getParentElement().getStyle().setProperty("borderTop", "0px");
 
 		// Affichage de la popup centrée
 		confirmationSuppressionPopup.center();
 
 	}
 
 	@UiHandler("poiSaveButton")
 	void onPoiSaveButtonClick(ClickEvent event) {
 		// Mise a jour du poi correspondant
 		if (this.poiSaveButton.getText().equals(CREER)) {
 			// Creation d'un poi à l'endroit cliqué (sinon on crée/hack un click au centre de la map si pas de click !)
 			MapWidget map = getMainPanel().getCartePanel().getGmapPanel().getMap();
 			if (this.mapCLickEvent == null) {
 				setCLickEvent(new MapClickEvent(map, null, map.getCenter(), null));
 			}
 
 			MarkerOptions options = MarkerOptions.newInstance();
 			options.setDraggable(true);
 
 			// Tooltip du poi sur la map
 			options.setTitle(this.poiName.getText().equals("") ? "(pas de nom)" : this.poiName.getText());
 
 			final CustomMarker newMarker = new CustomMarker(new Long(0), this.poiName.getText(),
 					this.mapCLickEvent.getLatLng(), this.poiCommentaire.getText(), getMainPanel().getCurrentUser(),
 					options);
 
 			newMarker.addMarkerClickHandler(new MarkerClickHandler() {
 				public void onClick(MarkerClickEvent event) {
 					setInfos(newMarker);
 				}
 			});
 
 			newMarker.addMarkerDragStartHandler(new MarkerDragStartHandler() {
 				public void onDragStart(MarkerDragStartEvent event) {
 					setInfos(newMarker);
 					newMarker.setDragStart(event.getSender().getLatLng());
 				}
 			});
 
 			newMarker.addMarkerDragEndHandler(new MarkerDragEndHandler() {
 				public void onDragEnd(MarkerDragEndEvent event) {
 					if (getMainPanel().getCurrentUser() != null && getMainPanel().getCurrentUser().isLogged()) {
 						setInfos(newMarker);
 					} else {
 						// Faire revenir le marker a son point de depart !!
 						event.getSender().setLatLng(newMarker.getDragStart());
 					}
 				}
 
 			});
 
 			// Mise à jour des infos de la PoiView
 			setInfos(newMarker);
 
 			// Affichage du poi sur la map
 			map.addOverlay(newMarker);
 
 			// Suppression de l'overlay du marker temporaire
 			getMainPanel().getCartePanel().getGmapPanel().getMap()
 					.removeOverlay(getMainPanel().getCartePanel().getGmapPanel().getTempMarker());
 
 			// Mise à jour dans le datastore
 			persistMarker(newMarker, CREER);
 
 		} else {
 			// Modification du poi
 			this.getCurrentPoi().setLibelle(this.poiName.getText());
 			this.getCurrentPoi().setCommentaire(this.poiCommentaire.getText());
 			this.getCurrentPoi().setLatLng(
 					LatLng.newInstance(new Double(this.poiLatitude.getText()).doubleValue(), new Double(
 							this.poiLongitude.getText()).doubleValue()));
 
 			persistMarker(this.getCurrentPoi(), MODIFIER);
 		}
 	}
 
 	private void persistMarker(CustomMarker customMarker, String mode) {
 
 		final AsyncCallback<PoiEntity> callback = new AsyncCallback<PoiEntity>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("persistMarker() - PersistentService RPC call failed : " + caught);
 			}
 
 			@Override
 			public void onSuccess(PoiEntity result) {
 				GWT.log("persistMarker() - PersistentService RPC call succedded for : " + result.toString());
 
 				// Mettre a jour l'id retourné par le service asynchrone dans le poi
 				PoiViewPanel.this.getCurrentPoi().setDatastoreId(result.getId());
 
 				// Mise à jour dans l'objet listePoiDataProvider (pour rafraichir la liste)
 				// On supprime et on ajoute l'objet pour eviter de l'avoir en double !!
 				getMainPanel().getCartePanel().getGmapPanel().getListePoiDataProvider().getList()
 						.remove(PoiViewPanel.this.getCurrentPoi());
 				getMainPanel().getCartePanel().getGmapPanel().getListePoiDataProvider().getList()
 						.add(PoiViewPanel.this.getCurrentPoi());
 			}
 		};
 
 		if (mode.equals(MODIFIER)) {
 			this.mainPanel.getPersistentService().getOrUpdatePoi(customMarker.getDatastoreId(),
 					customMarker.getLibelle(), customMarker.getGeoPt(), customMarker.getCommentaire(), new Date(),
 					getMainPanel().getCurrentUser(), callback);
 		} else { // mode CREER
 			this.mainPanel.getPersistentService().createPoi(customMarker.getLibelle(), customMarker.getGeoPt(),
 					customMarker.getCommentaire(), new Date(), getMainPanel().getCurrentUser(), callback);
 		}
 
 	}
 }
