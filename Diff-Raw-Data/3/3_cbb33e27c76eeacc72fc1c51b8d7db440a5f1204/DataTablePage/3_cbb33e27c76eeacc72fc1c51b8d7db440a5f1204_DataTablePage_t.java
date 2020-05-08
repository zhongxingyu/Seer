 package net.dontdrinkandroot.wicketexample.web.page.component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.metamodel.SingularAttribute;
 
 import net.dontdrinkandroot.wicket.bootstrap.component.pagination.AjaxPaginationPanel;
 import net.dontdrinkandroot.wicket.bootstrap.css.PaginationSize;
 import net.dontdrinkandroot.wicketexample.dao.TestEntityDao;
 import net.dontdrinkandroot.wicketexample.entity.TestEntity;
import net.dontdrinkandroot.wicketexample.entity.TestEntity_;
 import net.dontdrinkandroot.wicketexample.web.component.GenerateTestDataButton;
 import net.dontdrinkandroot.wicketexample.web.dataprovider.TestEntitySortableDataProvider;
 import net.dontdrinkandroot.wicketexample.web.model.entity.TestEntityIdStringModel;
 import net.dontdrinkandroot.wicketexample.web.model.entity.TestEntityIntegerFieldStringModel;
 import net.dontdrinkandroot.wicketexample.web.model.entity.TestEntityStringFieldModel;
 import net.dontdrinkandroot.wicketexample.web.page.DecoratorWidePage;
 
 import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 
 public class DataTablePage extends DecoratorWidePage<Void> {
 
 	@SpringBean
 	private TestEntityDao testEntityDao;
 
 
 	public DataTablePage(PageParameters parameters) {
 
 		super(parameters);
 	}
 
 
 	@Override
 	protected void onInitialize() {
 
 		super.onInitialize();
 
 		SortableDataProvider<TestEntity, SingularAttribute<? super TestEntity, ?>> dataProvider =
 				new TestEntitySortableDataProvider();
 
 		List<IColumn<TestEntity, SingularAttribute<? super TestEntity, ?>>> columns =
 				new ArrayList<IColumn<TestEntity, SingularAttribute<? super TestEntity, ?>>>();
 		columns.add(new AbstractColumn<TestEntity, SingularAttribute<? super TestEntity, ?>>(
 				new Model<String>("Id"),
 				TestEntity_.id) {
 
 			@Override
 			public void populateItem(
 					Item<ICellPopulator<TestEntity>> cellItem,
 					String componentId,
 					IModel<TestEntity> rowModel) {
 
 				cellItem.add(new Label(componentId, new TestEntityIdStringModel(rowModel)));
 			}
 
 		});
 		columns.add(new AbstractColumn<TestEntity, SingularAttribute<? super TestEntity, ?>>(new Model<String>(
 				"Integer Field"), TestEntity_.integerField) {
 
 			@Override
 			public void populateItem(
 					Item<ICellPopulator<TestEntity>> cellItem,
 					String componentId,
 					IModel<TestEntity> rowModel) {
 
 				cellItem.add(new Label(componentId, new TestEntityIntegerFieldStringModel(rowModel)));
 			}
 
 		});
 		columns.add(new AbstractColumn<TestEntity, SingularAttribute<? super TestEntity, ?>>(new Model<String>(
 				"String Field"), TestEntity_.stringField) {
 
 			@Override
 			public void populateItem(
 					Item<ICellPopulator<TestEntity>> cellItem,
 					String componentId,
 					IModel<TestEntity> rowModel) {
 
 				cellItem.add(new Label(componentId, new TestEntityStringFieldModel(rowModel)));
 			}
 
 		});
 
 		AjaxFallbackDefaultDataTable<TestEntity, SingularAttribute<? super TestEntity, ?>> dataTable =
 				new AjaxFallbackDefaultDataTable<TestEntity, SingularAttribute<? super TestEntity, ?>>(
 						"dataTable",
 						columns,
 						dataProvider,
 						10);
 		this.add(dataTable);
 
 		this.add(new GenerateTestDataButton("generateTestDataLink"));
 
 		this.add(new AjaxPaginationPanel("paginationPanel", dataTable, PaginationSize.SMALL));
 	}
 
 
 	@Override
 	public void renderHead(IHeaderResponse response) {
 
 		super.renderHead(response);
 	}
 }
