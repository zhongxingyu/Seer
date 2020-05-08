 package com.simu.ilearn.app.client.web.application.learn.widget;
 
 import com.google.common.base.Strings;
 import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 import com.simu.ilearn.app.client.web.application.learn.renderer.TagCellFactory;
 import com.simu.ilearn.app.client.web.application.learn.ui.LearnEditor;
import com.simu.ilearn.common.client.resource.style.TagListStyle;
 import com.simu.ilearn.common.shared.vo.LearnVO;
 import com.simu.ilearn.common.shared.vo.TagVO;
 
 import java.util.List;
 
 public class AddLearnView extends ViewWithUiHandlers<AddLearnUiHandlers> implements AddLearnPresenter.MyView {
     public interface Binder extends UiBinder<Widget, AddLearnView> {
     }
 
     @UiField(provided = true)
     LearnEditor learnEditor;
     @UiField(provided = true)
     SuggestBox tag;
     @UiField(provided = true)
     CellList<TagVO> tags;
     @UiField
     HTMLPanel addTagPanel;
 
     private final ListDataProvider<TagVO> dataProvider;
 
     @Inject
     public AddLearnView(final Binder uiBinder, LearnEditor learnEditor,
                        ListDataProvider<TagVO> dataProvider, TagCellFactory tagCellFactory, TagListStyle tagListStyle) {
         this.learnEditor = learnEditor;
         this.dataProvider = dataProvider;
         this.tag = new SuggestBox(new MultiWordSuggestOracle());
 
         this.tags = new CellList<TagVO>(tagCellFactory.create(new ActionCell.Delegate<TagVO>() {
             @Override
             public void execute(TagVO tagVO) {
                 removeTag(tagVO);
             }
        }), tagListStyle);
 
         initWidget(uiBinder.createAndBindUi(this));
 
         dataProvider.addDataDisplay(tags);
     }
 
     private void removeTag(TagVO tagVO) {
         dataProvider.getList().remove(tagVO);
     }
 
     @Override
     public void editLearn(LearnVO learn) {
         learnEditor.edit(learn);
         dataProvider.getList().clear();
     }
 
     @UiHandler("submit")
     public void onSaveClicked(ClickEvent event) {
         LearnVO learn = learnEditor.get();
         learn.setTags(dataProvider.getList());
         getUiHandlers().saveLearn(learn);
     }
 
     @UiHandler("tag")
     void onPasswordKeyUp(KeyUpEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !Strings.isNullOrEmpty(tag.getValue())) {
             TagVO tagVO = new TagVO();
             tagVO.setTitle(tag.getValue());
             dataProvider.getList().add(tagVO);
             addTagPanel.setVisible(false);
             tag.setValue("");
         }
     }
 
     @UiHandler("addTag")
     void onAddTagClick(ClickEvent event) {
         addTagPanel.setVisible(!addTagPanel.isVisible());
     }
 
     @Override
     public void initSuggestionList(List<TagVO> suggestions) {
         ((MultiWordSuggestOracle) tag.getSuggestOracle()).clear();
 
         if (suggestions != null && !suggestions.isEmpty()) {
             for (TagVO tagVO : suggestions) {
                 ((MultiWordSuggestOracle) tag.getSuggestOracle()).add(tagVO.getTitle());
             }
         }
     }
 }
