 package com.scholastic.sbam.client.util;
 
 import java.util.Date;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.CurrencyData;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.i18n.client.DefaultCurrencyData;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.AgreementTypeListService;
 import com.scholastic.sbam.client.services.AgreementTypeListServiceAsync;
 import com.scholastic.sbam.client.services.CancelReasonListService;
 import com.scholastic.sbam.client.services.CancelReasonListServiceAsync;
 import com.scholastic.sbam.client.services.CommissionTypeListService;
 import com.scholastic.sbam.client.services.CommissionTypeListServiceAsync;
 import com.scholastic.sbam.client.services.ContactTypeListService;
 import com.scholastic.sbam.client.services.ContactTypeListServiceAsync;
 import com.scholastic.sbam.client.services.DeleteReasonListService;
 import com.scholastic.sbam.client.services.DeleteReasonListServiceAsync;
 import com.scholastic.sbam.client.services.LinkTypeListService;
 import com.scholastic.sbam.client.services.LinkTypeListServiceAsync;
 import com.scholastic.sbam.client.services.ProductListService;
 import com.scholastic.sbam.client.services.ProductListServiceAsync;
 import com.scholastic.sbam.client.services.TermTypeListService;
 import com.scholastic.sbam.client.services.TermTypeListServiceAsync;
 import com.scholastic.sbam.client.stores.BetterFilterListStore;
 import com.scholastic.sbam.shared.objects.AgreementTypeInstance;
 import com.scholastic.sbam.shared.objects.AuthMethodInstance;
 import com.scholastic.sbam.shared.objects.CancelReasonInstance;
 import com.scholastic.sbam.shared.objects.CommissionTypeInstance;
 import com.scholastic.sbam.shared.objects.ContactTypeInstance;
 import com.scholastic.sbam.shared.objects.DeleteReasonInstance;
 import com.scholastic.sbam.shared.objects.GenericCodeInstance;
 import com.scholastic.sbam.shared.objects.LinkTypeInstance;
 import com.scholastic.sbam.shared.objects.ProductInstance;
 import com.scholastic.sbam.shared.objects.SimpleKeyProvider;
 import com.scholastic.sbam.shared.objects.TermTypeInstance;
 
 public class UiConstants {
 	public static final int				QUICK_TOOL_TIP_SHOW_DELAY		= 3000;
 	public static final int				QUICK_TOOL_TIP_DISMISS_DELAY	= 2000;
 	public static final DateTimeFormat	APP_DATE_LONG_FORMAT			= DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);
 	public static final DateTimeFormat	APP_DATE_TIME_FORMAT			= DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);	//	DateTimeFormat.getFormat("MM/dd/yy");
 	public static final DateTimeFormat	APP_DATE_PLUS_TIME_FORMAT		= DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
 	public static final CurrencyData	US_DOLLARS						= new DefaultCurrencyData("840", "$");
 	public static final NumberFormat	INTEGER_FORMAT					= NumberFormat.getFormat("#");
	public static final NumberFormat	BLANK_WHILE_ZERO				= NumberFormat.getFormat("BWZ");
 	public static final NumberFormat	DOLLARS_FORMAT					= NumberFormat.getCurrencyFormat(US_DOLLARS);
 	
 	/**
 	 * This enumeration helps to support the fact that different commission type lists will be delivered for different targets.
 	 * @author Bob Lacatena
 	 *
 	 */
 	public enum CommissionTypeTargets {
 		PRODUCT, SITE, AGREEMENT, AGREEMENT_TERM;
 	}
 	
 	private final static int			REFRESH_PERIOD = 10 * 60 * 1000;	// Every 10 minutes
 	
 	private static BetterFilterListStore<BeanModel>		agreementTypes	= new BetterFilterListStore<BeanModel>();
 	private static BetterFilterListStore<BeanModel>		commissionTypes = new BetterFilterListStore<BeanModel>();
 	private static BetterFilterListStore<BeanModel>		contactTypes	= new BetterFilterListStore<BeanModel>();
 	private static BetterFilterListStore<BeanModel>		deleteReasons	= new BetterFilterListStore<BeanModel>();
 	private static BetterFilterListStore<BeanModel>		cancelReasons 	= new BetterFilterListStore<BeanModel>();
 	private static BetterFilterListStore<BeanModel>		linkTypes		= new BetterFilterListStore<BeanModel>();
 	private static BetterFilterListStore<BeanModel>		products 		= new BetterFilterListStore<BeanModel>();
 	private static ListStore<BeanModel>					termTypes 		= new ListStore<BeanModel>();
 	
 	private static ListStore<BeanModel>					uidTypes		= getUidTypes();
 	private static ListStore<BeanModel>					authMethodTypes = getAuthMethodTypes();
 	
 	private static Timer								refreshTimer;
 	
 	public static void init() {
 		refresh();
 		setTimer();
 	}
 	
 	public static void refresh() {
 		loadAgreementTypes();
 		loadCommissionTypes();
 		loadContactTypes();
 		loadDeleteReasons();
 		loadCancelReasons();
 		loadLinkTypes();
 		loadProducts();
 		loadTermTypes();
 	}
 	
 	public static void setLoggedIn() {
 		init();
 	}
 	
 	public static void setLoggedOut() {
 		cancel();
 	}
 	
 	public static void setTimer() {
 		if (refreshTimer == null) {
 			refreshTimer = new Timer() {
 			      @Override
 			      public void run() {
 			        refresh();
 			      }
 			    };
 		}
 	
 	    // Schedule the timer to run once in 5 minutes.
 	    refreshTimer.schedule(REFRESH_PERIOD);
 
 	}
 	
 	public static void cancel() {
 		cancelTimer();
 	}
 	
 	public static void cancelTimer() {
 		if (refreshTimer != null)
 			refreshTimer.cancel();
 	}
 	
 	public static void loadTermTypes() {
 		TermTypeListServiceAsync termTypeListService = GWT.create(TermTypeListService.class);
 		
 		AsyncCallback<List<TermTypeInstance>> callback = new AsyncCallback<List<TermTypeInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Term types load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<TermTypeInstance> list) {
 				termTypes.removeAll();
 				for (TermTypeInstance instance : list) {
 					termTypes.add(TermTypeInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		termTypeListService.getTermTypes(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getTermTypes() {
 		return termTypes;
 	}
 	
 	public static void loadDeleteReasons() {
 		DeleteReasonListServiceAsync deleteReasonListService = GWT.create(DeleteReasonListService.class);
 		
 		AsyncCallback<List<DeleteReasonInstance>> callback = new AsyncCallback<List<DeleteReasonInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Delete reasons load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<DeleteReasonInstance> list) {
 				deleteReasons.removeAll();
 				
 				//	This is special... it doesn't exist in the database as an actual "code"
 				deleteReasons.add(DeleteReasonInstance.obtainModel(DeleteReasonInstance.getNoneActiveInstance()));
 				for (DeleteReasonInstance instance : list) {
 					deleteReasons.add(DeleteReasonInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		deleteReasonListService.getDeleteReasons(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getDeleteReasons() {
 		return deleteReasons;
 	}
 	
 	public static void loadAgreementTypes() {
 		AgreementTypeListServiceAsync agreementTypeListService = GWT.create(AgreementTypeListService.class);
 		
 		AsyncCallback<List<AgreementTypeInstance>> callback = new AsyncCallback<List<AgreementTypeInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "AgreementTypes load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<AgreementTypeInstance> list) {
 				agreementTypes.removeAll();
 				for (AgreementTypeInstance instance : list) {
 					agreementTypes.add(AgreementTypeInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		agreementTypeListService.getAgreementTypes(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getAgreementTypes() {
 		return agreementTypes;
 	}
 	
 	public static void loadCancelReasons() {
 		CancelReasonListServiceAsync cancelReasonListService = GWT.create(CancelReasonListService.class);
 		
 		AsyncCallback<List<CancelReasonInstance>> callback = new AsyncCallback<List<CancelReasonInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Cancel reasons load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<CancelReasonInstance> list) {
 				cancelReasons.removeAll();
 
 				//	This is special... it doesn't exist in the database as an actual "code"
 				cancelReasons.add(CancelReasonInstance.obtainModel(CancelReasonInstance.getNoneActiveInstance()));
 				for (CancelReasonInstance instance : list) {
 					cancelReasons.add(CancelReasonInstance.obtainModel(instance));	
 				}
 				
 			}
 		};
 		
 		cancelReasonListService.getCancelReasons(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getCancelReasons() {
 		return cancelReasons;
 	}
 	
 	public static void loadCommissionTypes() {
 		CommissionTypeListServiceAsync commissionTypeListService = GWT.create(CommissionTypeListService.class);
 		
 		AsyncCallback<List<CommissionTypeInstance>> callback = new AsyncCallback<List<CommissionTypeInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Commission types load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<CommissionTypeInstance> list) {
 				commissionTypes.removeAll();
 				if (commissionTypes.getKeyProvider() == null)
 					commissionTypes.setKeyProvider(new SimpleKeyProvider("commissionCode"));
 				for (CommissionTypeInstance instance : list) {
 					commissionTypes.add(CommissionTypeInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		commissionTypeListService.getCommissionTypes(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getCommissionTypes() {
 		return commissionTypes;
 	}
 	
 	public static void loadContactTypes() {
 		ContactTypeListServiceAsync contactTypeListService = GWT.create(ContactTypeListService.class);
 		
 		AsyncCallback<List<ContactTypeInstance>> callback = new AsyncCallback<List<ContactTypeInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Contact types load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<ContactTypeInstance> list) {
 				contactTypes.removeAll();
 				if (contactTypes.getKeyProvider() == null)
 					contactTypes.setKeyProvider(new SimpleKeyProvider("contactTypeCode"));
 				for (ContactTypeInstance instance : list) {
 					contactTypes.add(ContactTypeInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		contactTypeListService.getContactTypes(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getContactTypes() {
 		return contactTypes;
 	}
 	
 	public static void loadLinkTypes() {
 		LinkTypeListServiceAsync linkTypeListService = GWT.create(LinkTypeListService.class);
 		
 		AsyncCallback<List<LinkTypeInstance>> callback = new AsyncCallback<List<LinkTypeInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Link types load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<LinkTypeInstance> list) {
 				linkTypes.removeAll();
 				if (linkTypes.getKeyProvider() == null)
 					linkTypes.setKeyProvider(new SimpleKeyProvider("linkTypeCode"));
 				for (LinkTypeInstance instance : list) {
 					linkTypes.add(LinkTypeInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		linkTypeListService.getLinkTypes(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getLinkTypes() {
 		return linkTypes;
 	}
 	
 	public static void loadProducts() {
 		ProductListServiceAsync productListService = GWT.create(ProductListService.class);
 		
 		AsyncCallback<List<ProductInstance>> callback = new AsyncCallback<List<ProductInstance>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				if (caught instanceof IllegalArgumentException)
 					MessageBox.alert("Alert", caught.getMessage(), null);
 				else {
 					MessageBox.alert("Alert", "Products load failed unexpectedly.", null);
 					System.out.println(caught.getClass().getName());
 					System.out.println(caught.getMessage());
 				}
 			}
 
 			public void onSuccess(List<ProductInstance> list) {
 				products.removeAll();
 				for (ProductInstance instance : list) {
 					products.add(ProductInstance.obtainModel(instance));	
 				}
 			}
 		};
 		
 		productListService.getProducts(null, callback);
 	}
 	
 	public static ListStore<BeanModel> getProducts() {
 		return products;
 	}
 	
 	public static ListStore<BeanModel> getCommissionTypes(CommissionTypeTargets target) {
 		ListStore<BeanModel> list = new BetterFilterListStore<BeanModel>();
 		list.setKeyProvider(new SimpleKeyProvider("commissionTypeCode"));
 //		list.setKeyProvider(CommissionTypeInstance.obtainStaticModelKeyProvider());
 		
 		for (BeanModel model : commissionTypes.getModels()) {
 			CommissionTypeInstance instance = (CommissionTypeInstance) model.getBean();
 			if (target == CommissionTypeTargets.PRODUCT && instance.isProducts())
 				list.add(model);
 			else if (target == CommissionTypeTargets.SITE && instance.isSites())
 				list.add(model);
 			else if (target == CommissionTypeTargets.AGREEMENT && instance.isAgreements())
 				list.add(model);
 			else if (target == CommissionTypeTargets.AGREEMENT_TERM && instance.isAgreements())
 				list.add(model);
 		}
 		
 		return list;
 	}
 	
 	public static ListStore<BeanModel> getUidTypes() {
 		if (uidTypes != null)
 			return uidTypes;
 		ListStore<BeanModel> list = new ListStore<BeanModel>();
 		list.setKeyProvider(new SimpleKeyProvider("code"));
 		for (AuthMethodInstance.UserTypes type : AuthMethodInstance.UserTypes.values()) {
 			GenericCodeInstance instance = new GenericCodeInstance(type.getCode(), type.getName());
 			list.add(GenericCodeInstance.obtainModel(instance));
 		}
 		return list;
 	}
 	
 	public static ListStore<BeanModel> getAuthMethodTypes() {
 		if (authMethodTypes != null)
 			return authMethodTypes;
 		ListStore<BeanModel> list = new ListStore<BeanModel>();
 		list.setKeyProvider(new SimpleKeyProvider("code"));
 		
 		GenericCodeInstance instance;
 		
 		instance = new GenericCodeInstance(AuthMethodInstance.AM_IP, "IP");
 		list.add(GenericCodeInstance.obtainModel(instance));
 		
 		instance = new GenericCodeInstance(AuthMethodInstance.AM_UID, "UID");
 		list.add(GenericCodeInstance.obtainModel(instance));
 		
 		instance = new GenericCodeInstance(AuthMethodInstance.AM_URL, "URL");
 		list.add(GenericCodeInstance.obtainModel(instance));
 		
 		return list;
 	}
 
 	public static ToolTipConfig getQuickTip(String toolTip) {
 		ToolTipConfig config = new ToolTipConfig();
 		config.setText(toolTip);
 		config.setShowDelay(QUICK_TOOL_TIP_SHOW_DELAY);
 		config.setDismissDelay(QUICK_TOOL_TIP_DISMISS_DELAY);
 		config.setAnchor("right");
 		config.setAnchorOffset(0);
 		config.setAnchorToTarget(true);
 		return config;
 	}
 	
 	public static String formatDateLong(Date date) {
 		return APP_DATE_LONG_FORMAT.format(date);
 	}
 	
 	public static String formatDate(Date date) {
 		return APP_DATE_TIME_FORMAT.format(date);
 	}
 	
 	public static native String getUserAgent() /*-{
 	return navigator.userAgent.toLowerCase();
 	}-*/;
 	
 	public static boolean isInternetExplorer() {
 		return getUserAgent().contains("msie");
 	}
 }
