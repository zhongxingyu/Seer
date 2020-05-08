 package com.ecom.web.search;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.wicket.Session;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.mapper.parameter.PageParameters.NamedPair;
 import org.apache.wicket.request.resource.ResourceReference;
 import org.apache.wicket.util.string.Strings;
 
 import com.ecom.domain.OfferType;
 import com.ecom.domain.RealState;
 import com.ecom.domain.RealStateSort;
 import com.ecom.domain.RealStateType;
 import com.ecom.domain.SearchRequest;
 import com.ecom.web.components.buttons.MiniButton;
 import com.ecom.web.components.image.EcomImageResouceReference;
 import com.ecom.web.components.image.StaticImage;
 import com.ecom.web.components.pagination.CustomizedPagingNavigator;
 import com.ecom.web.components.stateless.StatelessAjaxFormComponentUpdatingBehavior;
 import com.ecom.web.data.RealStateDataProvider;
 import com.ecom.web.main.EcomSession;
 import com.ecom.web.main.GenericTemplatePage;
 import com.ecom.web.main.NewsletterPanel;
 
 public class SearchResultPage extends GenericTemplatePage {
 
 	private static final long serialVersionUID = -6983320790900379278L;
 
 	public SearchResultPage(final PageParameters params) {
 
 		setStatelessHint(true);
 
 		SearchRequest req = recreateSearchRequest(params);
 
 		final SortableDataProvider<RealState> dataProvider = new RealStateDataProvider(req);
 
 		final CompoundPropertyModel<SearchRequest> searchReqModel = new CompoundPropertyModel<SearchRequest>(req);
 
 		final WebMarkupContainer dataContainer = new WebMarkupContainer("dataContainer");
 		dataContainer.setOutputMarkupId(true);
 		dataContainer.setOutputMarkupPlaceholderTag(true);
 
 		final Form<SearchRequest> searchForm = new Form<SearchRequest>("searchForm", searchReqModel) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean getStatelessHint() {
 				return true;
 			}
 		};
 
 		RealStateSort sortOrder = RealStateSort.PRC_ASC;
 
 		if (req.getSortOrder() != null) {
 			sortOrder = req.getSortOrder();
 		}
 
 		IModel<RealStateSort> sortparamModel = new Model<RealStateSort>(sortOrder);
 		dataProvider.setSort(new SortParam(sortOrder.toString(), false));
 
 		final DropDownChoice<RealStateSort> sortResults = new DropDownChoice<RealStateSort>("sortResults", sortparamModel, Arrays.asList(RealStateSort
 				.values()), new EnumChoiceRenderer<RealStateSort>());
 		sortResults.add(new StatelessAjaxFormComponentUpdatingBehavior("onchange") {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected PageParameters getPageParameters() {
 				SearchRequest req = searchReqModel.getObject();
 				RealStateSort sortOrder = (RealStateSort) sortResults.getDefaultModelObject();
 				req.setSortOrder(sortOrder);
 				return createPageParameters(req);
 			}
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				SearchRequest req = searchReqModel.getObject();
 				RealStateSort sortOrder = (RealStateSort) sortResults.getDefaultModelObject();
 				req.setSortOrder(sortOrder);
 				searchReqModel.setObject(req);
 				PageParameters sortparams = createPageParameters(req);
 				setResponsePage(SearchResultPage.class, sortparams);
 			}
 
 		});
 		add(sortResults);
 
 		final TextField<Double> priceFromTxt = new TextField<Double>("priceFrom");
 		final TextField<Double> priceToTxt = new TextField<Double>("priceTo");
 		final TextField<Double> areaFromTxt = new TextField<Double>("areaFrom");
 		final TextField<Double> areaToTxt = new TextField<Double>("areaTo");
 		final TextField<Double> roomsFromTxt = new TextField<Double>("roomsFrom");
 		final TextField<Double> roomsToTxt = new TextField<Double>("roomsTo");
 		CheckBox isProvisionFree = new CheckBox("isProvisionFree");
 		CheckBox isKitchenAvailable = new CheckBox("isKitchenAvailable");
 		CheckBox isFurnished = new CheckBox("isFurnished");
 		CheckBox isBalconyAvailable = new CheckBox("isBalconyAvailable");
 		CheckBox isLiftAvailable = new CheckBox("isLiftAvailable");
 		CheckBox isGardenAvailable = new CheckBox("isGardenAvailable");
 
 		searchForm.add(priceFromTxt);
 		searchForm.add(priceToTxt);
 		searchForm.add(areaFromTxt);
 		searchForm.add(areaToTxt);
 		searchForm.add(roomsFromTxt);
 		searchForm.add(roomsToTxt);
 
 		searchForm.add(isProvisionFree);
 		searchForm.add(isKitchenAvailable);
 		searchForm.add(isFurnished);
 		searchForm.add(isBalconyAvailable);
 		searchForm.add(isLiftAvailable);
 		searchForm.add(isGardenAvailable);
 
 		searchForm.add(new Button("submitSearch") {
 
 			private static final long serialVersionUID = -8016115162670393962L;
 
 			@Override
 			public void onSubmit() {
 
 				SearchRequest req = searchForm.getModelObject();
 				req.setRoomsFrom(roomsFromTxt.getModelObject());
 				PageParameters params = createPageParameters(req);
 				setResponsePage(SearchResultPage.class, params);
 			}
 
 		});
 
 		BookMarkDisplayPanel bookmarkPanel = new BookMarkDisplayPanel("bookmark");
 		add(new MiniButton<String>("clearAll", new Model<String>("clear")) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				EcomSession session = (EcomSession) Session.get();
 				session.clearFavourites();
 				setResponsePage(SearchResultPage.class, params);
 			}
 
 			@Override
 			public boolean isVisible() {
 				EcomSession session = (EcomSession) Session.get();
 				return !session.getFavourites().isEmpty();
 			}
 		});
 
 		add(bookmarkPanel);
 
 		int currentPage;
 		DataView<RealState> dataView = getResultView(dataProvider, params);
 
 		// Select current page
 		currentPage = getCurrentPage(params);
		dataView.setItemsPerPage(3);
 		dataView.setCurrentPage(currentPage);
 
 		final CustomizedPagingNavigator pagingNavigator = new CustomizedPagingNavigator("pagingNavigator", dataView, SearchResultPage.class,
 				SearchResultPage.this.getPageParameters());
 
 		dataContainer.addOrReplace(dataView);
 		dataContainer.addOrReplace(pagingNavigator);
 		addOrReplace(dataContainer);
 		addOrReplace(searchForm);
 		add(new NewsletterPanel("newsLetter"));
 
 	}
 
 	private final DataView<RealState> getResultView(final ISortableDataProvider<RealState> dataProvider, final PageParameters params) {
 		DataView<RealState> dataView = new DataView<RealState>("searchResultsView", dataProvider) {
 			private static final long serialVersionUID = -8557003080882186607L;
 
 			@Override
 			protected void populateItem(Item<RealState> item) {
 				final RealState realState = (RealState) item.getModelObject();
 
 				PageParameters detailParam = new PageParameters();
 				detailParam.add("appartment-id", realState.getId().toString());
 				BookmarkablePageLink<String> detailImageLink = new BookmarkablePageLink<String>("detailImageLink", DetailViewPage.class, detailParam);
 				BookmarkablePageLink<String> detailTitleLink = new BookmarkablePageLink<String>("detailTitleLink", DetailViewPage.class, detailParam);
 
 				final ResourceReference imagesResourceReference = new EcomImageResouceReference();
 				final PageParameters imageParameters = new PageParameters();
 				String imageId = realState.getTitleThumbNailImage();
 				imageParameters.set("id", imageId);
 
 				CharSequence urlForImage = getRequestCycle().urlFor(imagesResourceReference, imageParameters);
 				StaticImage img = getTitleImageFromUrl(urlForImage.toString());
 
 				detailImageLink.add(img);
 				item.add(detailImageLink);
 
 				item.add(detailTitleLink.add(new Label("title", realState.getTitle())));
 				item.add(new Label("price", String.valueOf(realState.getCost())));
 				item.add(new Label("size", String.valueOf(realState.getSize())));
 				item.add(new Label("rooms", String.valueOf(realState.getTotalRooms())));
 
 				String addressInfo = realState.getAddressInfo();
 
 				item.add(new Label("address", addressInfo));
 
 				item.add(new MiniButton<String>("bookmark", new ResourceModel("btn_add_to_favourites")) {
 
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick() {
 						EcomSession session = (EcomSession) Session.get();
 						session.addToFavourites(realState.getId().toString(), realState.getTitle());
 						setResponsePage(SearchResultPage.class, params);
 					}
 
 				});
 
 				int labelId = 1;
 
 				if (realState.isKitchenAvailable()) {
 					item.add(new Label("label" + labelId, new ResourceModel("lbl_kitchen_available")));
 					labelId++;
 				}
 
 				if (realState.isBalconyAvailable()) {
 					item.add(new Label("label" + labelId, new ResourceModel("lbl_balcon_available")));
 					labelId++;
 				}
 
 				if (realState.isProvisionFree()) {
 					item.add(new Label("label" + labelId, new ResourceModel("lbl_provision_free")));
 					labelId++;
 				}
 
 				if (labelId <= 3 && realState.isToiletWithBathRoom()) {
 					item.add(new Label("label" + labelId, new ResourceModel("lbl_toilet_wc")));
 					labelId++;
 				}
 
 				if (labelId <= 3) {
 
 					for (; labelId <= 3; labelId++) {
 						item.add(new Label("label" + labelId, "").setVisible(false));
 					}
 				}
 			}
 
 		};
 
 		return dataView;
 	}
 
 	private SearchRequest recreateSearchRequest(PageParameters params) {
 		final SearchRequest req = new SearchRequest();
 
 		List<NamedPair> nameValueKey = params.getAllNamed();
 
 		for (NamedPair keyVal : nameValueKey) {
 
 			// skip if value from request parameter is null
 			if (keyVal.getValue() == null || keyVal.getValue().length() == 0) {
 				continue;
 			}
 
 			if (keyVal.getKey().equals("typeId")) {
 				req.setTypeId(OfferType.valueOf(Integer.valueOf(keyVal.getValue())));
 			}
 
 			if (keyVal.getKey().equals("realStateType")) {
 				req.setRealStateType(RealStateType.valueOf(Integer.valueOf(keyVal.getValue())));
 			}
 			
 			if (keyVal.getKey().equals("city")) {
 				req.setCityOrZip(keyVal.getValue());
 			}
 
 			if (keyVal.getKey().equals("areaFrom")) {
 				req.setAreaFrom(Double.valueOf(keyVal.getValue()));
 			}
 
 			if (keyVal.getKey().equals("areaTo")) {
 				req.setAreaTo(Double.valueOf(keyVal.getValue()));
 			}
 
 			if (keyVal.getKey().equals("priceFrom")) {
 				req.setPriceFrom(Double.valueOf(keyVal.getValue()));
 			}
 
 			if (keyVal.getKey().equals("priceTo")) {
 				req.setPriceTo(Double.valueOf(keyVal.getValue()));
 			}
 
 			if (keyVal.getKey().equals("roomsFrom")) {
 				req.setRoomsFrom(Double.valueOf(keyVal.getValue()));
 			}
 
 			if (keyVal.getKey().equals("roomsTo")) {
 				req.setRoomsTo(Double.valueOf(keyVal.getValue()));
 			}
 
 			if (keyVal.getKey().equals("provFree")) {
 				String value = keyVal.getValue();
 				if (Boolean.valueOf(value) == true)
 					req.setProvisionFree(true);
 			}
 
 			if (keyVal.getKey().equals("kitchenAvail")) {
 				String value = keyVal.getValue();
 				if (Boolean.valueOf(value) == true)
 					req.setKitchenAvailable(true);
 			}
 
 			if (keyVal.getKey().equals("furnished")) {
 				String value = keyVal.getValue();
 				if (Boolean.valueOf(value) == true)
 					req.setFurnished(true);
 			}
 
 			if (keyVal.getKey().equals("balcon")) {
 				String value = keyVal.getValue();
 				if (Boolean.valueOf(value) == true)
 					req.setBalconyAvailable(true);
 			}
 
 			if (keyVal.getKey().equals("liftAvail")) {
 				String value = keyVal.getValue();
 				if (Boolean.valueOf(value) == true)
 					req.setLiftAvailable(true);
 			}
 
 			if (keyVal.getKey().equals("gardenAvail")) {
 				String value = keyVal.getValue();
 				if (Boolean.valueOf(value) == true)
 					req.setGardenAvailable(true);
 			}
 
 			if (keyVal.getKey().equals("sortOrder")) {
 				String value = keyVal.getValue();
 				req.setSortOrder(RealStateSort.getSort(value));
 			}
 		}
 
 		return req;
 	}
 
 	public PageParameters createPageParameters(SearchRequest req) {
 		PageParameters params = new PageParameters();
 
 		if (req.getRoomsFrom() != null) {
 			params.set("roomsFrom", req.getRoomsFrom());
 		}
 
 		if (req.getRoomsTo() != null) {
 			params.set("roomsTo", req.getRoomsTo());
 		}
 
 		if (req.getPriceFrom() != null) {
 			params.set("priceFrom", req.getPriceFrom());
 		}
 
 		if (req.getPriceTo() != null) {
 			params.set("priceTo", req.getPriceTo());
 		}
 
 		if (req.getAreaFrom() != null) {
 			params.set("areaFrom", req.getAreaFrom());
 		}
 
 		if (req.getAreaTo() != null) {
 			params.set("areaTo", req.getAreaTo());
 		}
 
 		if (req.getCityOrZip() != null) {
 			params.set("city", Strings.escapeMarkup(req.getCityOrZip()));
 		}
 
 		if (req.getSortOrder() != null) {
 			params.set("sortOrder", req.getSortOrder());
 		}
 
 		if (req.isProvisionFree() != null && req.isProvisionFree()) {
 			params.set("provFree", req.isProvisionFree());
 		}
 
 		if (req.isKitchenAvailable() != null && req.isKitchenAvailable()) {
 			params.set("kitchenAvail", req.isKitchenAvailable());
 		}
 
 		if (req.isFurnished() != null && req.isFurnished()) {
 			params.set("furnished", req.isFurnished());
 		}
 
 		if (req.isBalconyAvailable() != null && req.isBalconyAvailable()) {
 			params.set("balcon", req.isBalconyAvailable());
 		}
 
 		if (req.isLiftAvailable() != null && req.isLiftAvailable()) {
 			params.set("liftAvail", req.isLiftAvailable());
 		}
 
 		if (req.isGardenAvailable() != null && req.isGardenAvailable()) {
 			params.set("gardenAvail", req.isGardenAvailable());
 		}
 
 		return params;
 	}
 
 	private int getCurrentPage(PageParameters params) {
 
 		if (params.get(CustomizedPagingNavigator.PAGE_QUERY_ID) != null && !params.get(CustomizedPagingNavigator.PAGE_QUERY_ID).isEmpty()) {
 			return params.get(CustomizedPagingNavigator.PAGE_QUERY_ID).toInt() - CustomizedPagingNavigator.START_INDEX_POSITION;
 		}
 		return 0;
 
 	}
 
 }
