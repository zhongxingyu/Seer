 /*
  * BrowseWidget.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.client.browse;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Set;
 
 import com.google.gwt.event.logical.shared.ShowRangeEvent;
 import com.google.gwt.event.logical.shared.ShowRangeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.rpc.IsSerializable;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.prealpha.dispatch.shared.DispatcherAsync;
 import com.prealpha.extempdb.client.error.ManagedCallback;
 import com.prealpha.extempdb.client.taginput.LoadingStatus;
 import com.prealpha.extempdb.client.taginput.TagInputPresenter;
 import com.prealpha.extempdb.shared.action.GetMappingsByTag;
 import com.prealpha.extempdb.shared.action.GetMappingsResult;
 import com.prealpha.extempdb.shared.action.GetTag;
 import com.prealpha.extempdb.shared.action.GetTagResult;
 import com.prealpha.extempdb.shared.dto.ArticleDto;
 import com.prealpha.extempdb.shared.dto.TagDto;
 import com.prealpha.extempdb.shared.dto.TagMappingDto;
 import com.prealpha.extempdb.shared.dto.TagMappingDto.State;
 
 public class BrowseWidget extends Composite implements BrowsePresenter.Display {
 	public static interface BrowseUiBinder extends
 			UiBinder<Widget, BrowseWidget> {
 	}
 
 	@UiField(provided = true)
 	final Widget inputWidget;
 
 	@UiField(provided = true)
 	final MappingStateSelector stateSelector;
 
 	@UiField(provided = true)
 	final Widget articleTable;
 
 	@UiField(provided = true)
 	final SimpleEventPager pager;
 
 	private final TagInputPresenter inputPresenter;
 
 	private final ArticleTablePresenter tablePresenter;
 
 	private final DispatcherAsync dispatcher;
 
 	private BrowseState browseState;
 
 	@Inject
 	public BrowseWidget(BrowseUiBinder uiBinder,
 			final TagInputPresenter inputPresenter,
 			MappingStateSelector stateSelector,
 			ArticleTablePresenter tablePresenter, SimpleEventPager pager,
 			DispatcherAsync dispatcher) {
 		this.inputPresenter = inputPresenter;
 		this.stateSelector = stateSelector;
 		this.tablePresenter = tablePresenter;
 		this.pager = pager;
 		this.dispatcher = dispatcher;
 		inputWidget = inputPresenter.getDisplay().asWidget();
 		articleTable = tablePresenter.getDisplay().asWidget();
 		pager.setDisplay(tablePresenter.getDisplay().getDataDisplay());
 		initWidget(uiBinder.createAndBindUi(this));
 
 		inputPresenter
 				.addValueChangeHandler(new ValueChangeHandler<LoadingStatus>() {
 					@Override
 					public void onValueChange(
 							ValueChangeEvent<LoadingStatus> event) {
						/*
						 * Don't use inputPresenter.getTagName(), because that
						 * returns a non-null value if the tag doesn't exist.
						 */
 						String oldTagName = browseState.getTagName();
						TagDto tag = (event.getValue().isLoaded() ? inputPresenter
								.getTag() : null);
						String tagName = (tag == null ? null : tag.getName());
 
 						BrowseState newState = BrowseState.getInstance(tagName,
 								browseState.getStates(), browseState.getSort(),
 								0);
 
 						/*
 						 * setValue() calls inputPresenter.bind(), so we have to
 						 * check for equality here to prevent an infinite loop.
 						 */
 						if (oldTagName == null) {
 							if (tagName != null) {
 								setValue(newState, true);
 							}
 						} else if (!oldTagName.equals(tagName)) {
 							setValue(newState, true);
 						}
 					}
 				});
 
 		stateSelector
 				.addValueChangeHandler(new ValueChangeHandler<Set<State>>() {
 					@Override
 					public void onValueChange(ValueChangeEvent<Set<State>> event) {
 						BrowseState newState = BrowseState.getInstance(
 								browseState.getTagName(), event.getValue(),
 								browseState.getSort(), 0);
 						setValue(newState, true);
 					}
 				});
 
 		tablePresenter.getDisplay().addValueChangeHandler(
 				new ValueChangeHandler<ArticleSort>() {
 					@Override
 					public void onValueChange(
 							ValueChangeEvent<ArticleSort> event) {
 						BrowseState newState = BrowseState.getInstance(
 								browseState.getTagName(),
 								browseState.getStates(), event.getValue(), 0);
 						setValue(newState, true);
 					}
 				});
 
 		pager.addShowRangeHandler(new ShowRangeHandler<Integer>() {
 			@Override
 			public void onShowRange(ShowRangeEvent<Integer> event) {
 				BrowseState newState = BrowseState.getInstance(
 						browseState.getTagName(), browseState.getStates(),
 						browseState.getSort(), event.getStart());
 				setValue(newState, true);
 			}
 		});
 	}
 
 	@Override
 	public BrowseState getValue() {
 		return browseState;
 	}
 
 	@Override
 	public void setValue(BrowseState browseState) {
 		setValue(browseState, false);
 	}
 
 	@Override
 	public void setValue(final BrowseState browseState, boolean fireEvents) {
 		BrowseState oldBrowseState = this.browseState;
 		this.browseState = browseState;
 
 		String tagName = browseState.getTagName();
 		if (tagName == null) {
 			inputPresenter.bind(null);
 			tablePresenter.bind(Collections.<TagMappingDto.Key> emptyList());
 		} else {
 			GetTag tagAction = new GetTag(tagName);
 			dispatcher.execute(tagAction, new ManagedCallback<GetTagResult>() {
 				@Override
 				public void onSuccess(GetTagResult result) {
 					TagDto tag = result.getTag();
 
 					inputPresenter.bind(tag);
 
 					Comparator<TagMappingDto> comparator = new ComparatorAdapter(
 							browseState.getSort());
 					GetMappingsByTag mappingsAction = new GetMappingsByTag(tag
 							.getName(), browseState.getStates(), comparator);
 					dispatcher.execute(mappingsAction,
 							new ManagedCallback<GetMappingsResult>() {
 								@Override
 								public void onSuccess(GetMappingsResult result) {
 									tablePresenter.bind(result.getMappingKeys());
 								}
 							});
 				}
 			});
 		}
 
 		stateSelector.setValue(browseState.getStates());
 		tablePresenter.getDisplay().setValue(browseState.getSort());
 		pager.setPageStart(browseState.getPageStart());
 
 		if (fireEvents) {
 			ValueChangeEvent.fireIfNotEqual(this, oldBrowseState, browseState);
 		}
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<BrowseState> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 
 	// package visibility for serialization support
 	static class ComparatorAdapter implements Comparator<TagMappingDto>,
 			IsSerializable {
 		private Comparator<? super ArticleDto> delegate;
 
 		// serialization support
 		@SuppressWarnings("unused")
 		private ComparatorAdapter() {
 		}
 
 		public ComparatorAdapter(Comparator<? super ArticleDto> delegate) {
 			assert (delegate != null);
 			this.delegate = delegate;
 		}
 
 		@Override
 		public int compare(TagMappingDto m1, TagMappingDto m2) {
 			return delegate.compare(m1.getArticle(), m2.getArticle());
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((delegate == null) ? 0 : delegate.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (!(obj instanceof ComparatorAdapter)) {
 				return false;
 			}
 			ComparatorAdapter other = (ComparatorAdapter) obj;
 			if (delegate == null) {
 				if (other.delegate != null) {
 					return false;
 				}
 			} else if (!delegate.equals(other.delegate)) {
 				return false;
 			}
 			return true;
 		}
 	}
 }
