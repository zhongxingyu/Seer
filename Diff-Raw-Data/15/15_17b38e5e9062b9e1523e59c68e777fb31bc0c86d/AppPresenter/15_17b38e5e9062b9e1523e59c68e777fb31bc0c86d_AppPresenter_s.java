 package fr.hors.limite.photo.client.presenter;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.AnchorElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 import fr.hors.limite.photo.client.model.HlpModel;
 import fr.hors.limite.photo.client.presenter.command.CommandCoordPresenter;
 import fr.hors.limite.photo.client.presenter.command.CommandPaiementPresenter;
 import fr.hors.limite.photo.client.presenter.command.CommandPaypalCancelPresenter;
 import fr.hors.limite.photo.client.presenter.command.CommandPresenter;
 import fr.hors.limite.photo.client.presenter.command.CommandRecapPresenter;
 import fr.hors.limite.photo.client.presenter.contact.ContactPresenter;
 import fr.hors.limite.photo.client.presenter.event.EventsPresenter;
 import fr.hors.limite.photo.client.presenter.galery.GaleryPresenter;
 import fr.hors.limite.photo.client.presenter.price.PricePresenter;
 import fr.hors.limite.photo.client.presenter.weeding.WeedingLoginPresenter;
 import fr.hors.limite.photo.client.presenter.weeding.WeedingPresenter;
 import fr.hors.limite.photo.client.service.event.AddToCartEventHandler;
 import fr.hors.limite.photo.client.service.event.AddToCartEventHandler.AddToCartEvent;
 import fr.hors.limite.photo.client.service.event.CommandOrderEventHandler;
 import fr.hors.limite.photo.client.service.event.CommandOrderEventHandler.CommandOrderEvent;
 import fr.hors.limite.photo.client.util.CookieManager;
 import fr.hors.limite.photo.client.widget.CartWidget;
 import fr.hors.limite.photo.client.widget.MenuWidget;
 import fr.hors.limite.photo.common.client.event.HlpEventBus;
 import fr.hors.limite.photo.common.client.event.HlpResponseEvent;
 import fr.hors.limite.photo.common.client.event.HttpErrorEvent;
 import fr.hors.limite.photo.common.client.event.HttpErrorEventHandler;
 import fr.hors.limite.photo.common.client.event.menu.MenusEventHandler;
 import fr.hors.limite.photo.common.client.event.menu.MenusEventHandler.MenusEvent;
 import fr.hors.limite.photo.common.client.event.price.FormatPricesEventHandler;
 import fr.hors.limite.photo.common.client.event.price.FormatPricesEventHandler.FormatPricesEvent;
 import fr.hors.limite.photo.common.client.event.price.PhotoFormatsEventHandler;
 import fr.hors.limite.photo.common.client.event.price.PhotoFormatsEventHandler.PhotoFormatsEvent;
 import fr.hors.limite.photo.common.client.event.weeding.WeedingLoginEventHandler;
 import fr.hors.limite.photo.common.client.event.weeding.WeedingLoginEventHandler.WeedingLoginEvent;
 import fr.hors.limite.photo.common.client.model.HlpCommand;
 import fr.hors.limite.photo.common.client.model.HlpFormatPrice;
 import fr.hors.limite.photo.common.client.model.HlpMenu;
 import fr.hors.limite.photo.common.client.model.HlpOrderItem;
 import fr.hors.limite.photo.common.client.model.HlpPhotoFormat;
 import fr.hors.limite.photo.common.client.model.HlpWeeding;
 import fr.hors.limite.photo.common.client.presenter.DefaultPresenter;
 import fr.hors.limite.photo.common.client.presenter.Presenter;
 import fr.hors.limite.photo.common.client.service.HlpQueryParameters;
 import fr.hors.limite.photo.common.client.service.HlpService;
 import fr.hors.limite.photo.common.client.util.ServerUri;
 
 public class AppPresenter implements Presenter, ValueChangeHandler<String> {
 
 	interface AppStyle extends CssResource {
 
 		String cartWidget();
 	}
 
 	interface AppResource extends ClientBundle {
 		@Source("app.css")
 		AppStyle style();
 	}
 
 	private static final AppResource RESOURCE = GWT.create(AppResource.class);
 	private static final AppStyle STYLE = RESOURCE.style();
 	private final HlpEventBus eventBus = new HlpEventBus();
 	private final HlpEventBus loginBus = new HlpEventBus();
 	private final HlpModel model = new HlpModel();
 	private AnchorElement selectedMenu = null;
 	private final CartWidget cartWidget = new CartWidget(eventBus, model);
 	private List<HlpOrderItem> orderList;
 	private int nbRequest;
 	private final AppView view;
 	DefaultPresenter<?> presenter = null;
 	private DefaultPresenter<?> currentPresenter;
 
 	public AppPresenter() {
 		this.view = new AppView();
 		STYLE.ensureInjected();
 		History.addValueChangeHandler(this);
 		view.getNorthPanel().add(cartWidget);
 		cartWidget.setStyleName(STYLE.cartWidget());
 		cartWidget.setVisible(false);
 		bind();
 		RootLayoutPanel.get().add(view);
 	}
 
 	private void bind() {
 		eventBus.addHandler(FormatPricesEvent.TYPE, new FormatPricesEventHandler() {
 			@Override
 			public void onFormatPricesGetted(JsArray<HlpFormatPrice> formatPrices) {
 				model.initFormatPrice(formatPrices);
 				init();
 			}
 		});
 		eventBus.addHandler(HttpErrorEvent.TYPE, new HttpErrorEventHandler() {
 			@Override
 			public void onHttpError(HttpErrorEvent httpErrorEvent) {
				Window.alert(httpErrorEvent.getErrorMessage());
 			}
 		});
 		eventBus.addHandler(PhotoFormatsEvent.TYPE, new PhotoFormatsEventHandler() {
 
 			@Override
 			public void onPhotoFormatsGetted(JsArray<HlpPhotoFormat> photoFormats) {
 				model.initPhotoFormat(photoFormats);
 				init();
 			}
 		});
 		eventBus.addHandler(MenusEvent.TYPE, new MenusEventHandler() {
 			@Override
 			public void onMenusGetted(JsArray<HlpMenu> menus) {
 				addMenu("top", "north", menus);
 				addMenu("bottom", "south", menus);
 				init();
 			}
 		});
 		eventBus.addHandler(AddToCartEvent.TYPE, new AddToCartEventHandler() {
 
 			@Override
 			public void onAddToCart(AddToCartEvent addToCartEvent) {
 				final HlpOrderItem orderItem = addToCartEvent.getOrderItem();
 				final int photoFormat = orderItem.getPhotoFormat();
 				if (photoFormat < 0)
 					orderItem.setPhotoFormat(model.getDefaultPhotoFormat().getId());
 				orderItem.setTotal(model.getPrice(orderItem.getQuantity()));
 				cartWidget.addOrderItem(orderItem);
 				model.addOrder(orderItem);
 			}
 		});
 		eventBus.addHandler(CommandOrderEvent.TYPE, new CommandOrderEventHandler() {
 
 			@Override
 			public void onRecapCommand(CommandOrderEvent commandOrderEvent) {
 				orderList = commandOrderEvent.getOrders();
 				History.newItem(HlpQueryParameters.createQueryString("command", "list"));
 			}
 		});
 		loginBus.addHandler(WeedingLoginEvent.TYPE, new WeedingLoginEventHandler() {
 
 			@Override
 			public void onLogin(HlpWeeding weeding) {
 				model.setWeeding(weeding);
 				History.newItem(HlpQueryParameters.createQueryString("weeding", "list"));
 			}
 		});
 		this.view.getLogo().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("");
 				Window.Location.reload();
 			}
 		});
 	}
 
 	private void addMenu(String position, String domId, JsArray<HlpMenu> menus) {
 		if (menus != null) {
 			JsArray<HlpMenu> positionMenus = JavaScriptObject.createArray().cast();
 			for (int i = 0; i < menus.length(); i++) {
 				HlpMenu hlpMenu = menus.get(i);
 				if (hlpMenu.getPosition().equals(position))
 					positionMenus.push(menus.get(i));
 			}
 			MenuWidget menu = new MenuWidget(positionMenus, new MenuWidget.MenuSelected() {
 				@Override
 				public void onMenuSelected(AnchorElement selectedElement) {
 					if (selectedMenu != null)
 						selectedMenu.removeClassName(MenuWidget.ACTIVE_MENU_STYLE);
 					selectedMenu = selectedElement;
 				}
 			});
 			view.getSouthPanel().add(menu);
 		}
 	}
 
 	private void init() {
 		if (--nbRequest == 0) {
 			readCommandFromCookie();
 			showPresenter(History.getToken());
 		}
 	}
 
 	@Override
 	public void show(Panel parent) {
 		nbRequest = 3;
 		HlpService.get(ServerUri.MENUS, eventBus, new MenusEvent());
 		HlpService.get(ServerUri.PRICE_PHOTO_FORMATS, eventBus, new PhotoFormatsEvent());
 		HlpService.get(ServerUri.PRICE_FORMAT_PRICES, eventBus, new FormatPricesEvent());
 	}
 
 	private void readCommandFromCookie() {
 		final String cookie = CookieManager.getCommand();
 		model.clear();
 		if (cookie != null) {
 			model.setCommand(HlpResponseEvent.asJson(cookie).<HlpCommand> cast());
 			if (model.getCommand() != null && model.getCommand().getOrderItems() != null)
 				for (int i = 0; i < model.getCommand().getOrderItems().length(); i++)
 					cartWidget.addOrderItem(model.getCommand().getOrderItems().get(i));
 		} else {
 			final HlpCommand command = (HlpCommand) HlpCommand.createObject().cast();
 			model.setCommand(command);
 			CookieManager.saveCommand(command);
 		}
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String query = event.getValue();
 		showPresenter(query);
 	}
 
 	private void showPresenter(String query) {
 		HlpQueryParameters hlpQueryParameters = new HlpQueryParameters(query);
 		cartWidget.setVisible(false);
 		if (this.presenter != null)
 			view.remove(this.presenter.getDisplay());
 		if ("galery".equals(hlpQueryParameters.getModule()))
 			presenter = new GaleryPresenter(eventBus);
 		else if ("event".equals(hlpQueryParameters.getModule())) {
 			presenter = new EventsPresenter(eventBus);
 			cartWidget.setVisible(true);
 		} else if ("command".equals(hlpQueryParameters.getModule())) {
 			if ("coord".equals(hlpQueryParameters.getAction()))
 				presenter = new CommandCoordPresenter(eventBus, model);
 			else if ("recap".equals(hlpQueryParameters.getAction()))
 				presenter = new CommandRecapPresenter(eventBus, model, hlpQueryParameters);
 			else if ("paiement".equals(hlpQueryParameters.getAction()))
 				presenter = new CommandPaiementPresenter(eventBus, model);
 			else if ("cancel".equals(hlpQueryParameters.getAction()))
 				presenter = new CommandPaypalCancelPresenter(hlpQueryParameters, eventBus);
 			else
 				presenter = new CommandPresenter(eventBus, orderList, model);
 			cartWidget.setVisible(true);
 		} else if ("contact".equals(hlpQueryParameters.getModule()))
 			presenter = new ContactPresenter(eventBus);
 		else if ("weeding".equals(hlpQueryParameters.getModule())) {
 			if ("login".equals(hlpQueryParameters.getAction())) {
 				final String credentialValue = CookieManager.getWeeding();
 				if (credentialValue != null) {
 					final String[] challengeReponse = credentialValue.split(",");
 					HlpService.loginWeeding(challengeReponse[0], challengeReponse[1], loginBus, new WeedingLoginEventHandler.WeedingLoginEvent());
 				} else
 					presenter = new WeedingLoginPresenter(eventBus, model);
 			} else if ("list".equals(hlpQueryParameters.getAction())) {
 				presenter = new WeedingPresenter(eventBus, model);
 				cartWidget.setVisible(true);
 			}
 		} else if ("price".equals(hlpQueryParameters.getModule()))
 			presenter = new PricePresenter(eventBus);
 		if (presenter != null) {
 			if (currentPresenter != null)
 				currentPresenter.removeHandlers();
 			currentPresenter = presenter;
 			view.add(presenter.getDisplay());
 		}
 	}
 }
