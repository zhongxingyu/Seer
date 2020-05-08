 /*
  * BrowseWidget.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.client.jump;
 
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
 import com.prealpha.extempdb.client.taginput.TagInputWidget;
 import com.prealpha.extempdb.shared.action.GetMappingsByTag;
 import com.prealpha.extempdb.shared.action.GetMappingsResult;
 import com.prealpha.extempdb.shared.action.GetTag;
 import com.prealpha.extempdb.shared.action.GetTagResult;
 import com.prealpha.extempdb.shared.dto.ArticleDto;
 import com.prealpha.extempdb.shared.dto.TagDto;
 import com.prealpha.extempdb.shared.dto.TagMappingDto;
 import com.prealpha.extempdb.shared.dto.TagMappingDto.State;
 
 public class JumpWidget extends Composite implements JumpPresenter.Display {
 	public static interface JumpUiBinder extends UiBinder<Widget, JumpWidget> {
 	}
 
 	@UiField(provided = true)
 	final TagInputWidget tagInput;
 
 	@UiField(provided = true)
 	final MappingStateSelector stateSelector;
 
 	@UiField(provided = true)
 	final Widget articleTable;
 
 	@UiField(provided = true)
 	final SimpleEventPager pager;
 
 	private final ArticleTablePresenter tablePresenter;
 
 	private final DispatcherAsync dispatcher;
 
 	private JumpState jumpState;
 
 	@Inject
 	public JumpWidget(JumpUiBinder uiBinder, TagInputWidget tagInput,
 			MappingStateSelector stateSelector,
 			ArticleTablePresenter tablePresenter, SimpleEventPager pager,
 			DispatcherAsync dispatcher) {
 		this.tagInput = tagInput;
 		this.stateSelector = stateSelector;
 		this.tablePresenter = tablePresenter;
 		this.pager = pager;
 		this.dispatcher = dispatcher;
 		articleTable = tablePresenter.getDisplay().asWidget();
 		pager.setDisplay(tablePresenter.getDisplay().getDataDisplay());
 		initWidget(uiBinder.createAndBindUi(this));
 
 		/*
 		 * TODO: in the old code, there's an equality check; is it needed?
 		 */
 		tagInput.addValueChangeHandler(new ValueChangeHandler<TagDto>() {
 			@Override
 			public void onValueChange(ValueChangeEvent<TagDto> event) {
 				TagDto tag = event.getValue();
				JumpState newState = JumpState.getInstance(tag.getName(),
 						jumpState.getStates(), jumpState.getSort(), 0);
 				setValue(newState, true);
 			}
 		});
 
 		stateSelector
 				.addValueChangeHandler(new ValueChangeHandler<Set<State>>() {
 					@Override
 					public void onValueChange(ValueChangeEvent<Set<State>> event) {
 						JumpState newState = JumpState.getInstance(
 								jumpState.getTagName(), event.getValue(),
 								jumpState.getSort(), 0);
 						setValue(newState, true);
 					}
 				});
 
 		tablePresenter.getDisplay().addValueChangeHandler(
 				new ValueChangeHandler<ArticleSort>() {
 					@Override
 					public void onValueChange(
 							ValueChangeEvent<ArticleSort> event) {
 						JumpState newState = JumpState.getInstance(
 								jumpState.getTagName(), jumpState.getStates(),
 								event.getValue(), 0);
 						setValue(newState, true);
 					}
 				});
 
 		pager.addShowRangeHandler(new ShowRangeHandler<Integer>() {
 			@Override
 			public void onShowRange(ShowRangeEvent<Integer> event) {
 				JumpState newState = JumpState.getInstance(
 						jumpState.getTagName(), jumpState.getStates(),
 						jumpState.getSort(), event.getStart());
 				setValue(newState, true);
 			}
 		});
 	}
 
 	@Override
 	public JumpState getValue() {
 		return jumpState;
 	}
 
 	@Override
 	public void setValue(JumpState jumpState) {
 		setValue(jumpState, false);
 	}
 
 	@Override
 	public void setValue(final JumpState jumpState, boolean fireEvents) {
 		JumpState oldJumpState = this.jumpState;
 		this.jumpState = jumpState;
 
 		String tagName = jumpState.getTagName();
		if (tagName == null) {
			// TODO: maybe just use the old value?
			tagInput.setValue(null);
			tablePresenter.bind(Collections.<TagMappingDto.Key> emptyList());
		} else {
 			GetTag tagAction = new GetTag(tagName);
 			dispatcher.execute(tagAction, new ManagedCallback<GetTagResult>() {
 				@Override
 				public void onSuccess(GetTagResult result) {
 					TagDto tag = result.getTag();
 					tagInput.setValue(tag);
 
 					Comparator<TagMappingDto> comparator = new ComparatorAdapter(
 							jumpState.getSort());
 					GetMappingsByTag mappingsAction = new GetMappingsByTag(tag
 							.getName(), jumpState.getStates(), comparator);
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
 
 		stateSelector.setValue(jumpState.getStates());
 		tablePresenter.getDisplay().setValue(jumpState.getSort());
 		pager.setPageStart(jumpState.getPageStart());
 
 		if (fireEvents) {
 			ValueChangeEvent.fireIfNotEqual(this, oldJumpState, jumpState);
 		}
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<JumpState> handler) {
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
