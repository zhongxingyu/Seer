 package com.svanberg.household.web.pages;
 
 import com.svanberg.household.domain.Expense;
 import com.svanberg.household.service.ExpenseService;
 import com.svanberg.household.web.components.DomainProvider;
 import com.svanberg.household.web.components.specific.AddExpenseDialog;
 import com.svanberg.household.web.components.stateless.SelectColumn;
 import com.svanberg.household.web.components.stateless.SortableHeadersToolbar;
 import com.svanberg.household.web.components.stateless.StatelessPagingNavigator;
 
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.StatelessForm;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class ExpensePage extends HouseholdPage {
     private static final long serialVersionUID = -7046290354423141576L;
 
     static final String FORM = "selection";
     static final String DELETE = "delete";
     static final String TABLE = "table";
     static final String PAGINATION = "pagination";
     static final long ROWS_PER_PAGE = 20L;
 
     private @SpringBean ExpenseService expenseService;
 
     private final SelectColumn<Expense,String> selectColumn = new SelectColumn<>();
     private StatelessForm<Void> form;
 
     public ExpensePage(PageParameters parameters) {
         super(parameters);
 
         AddExpenseDialog dialog = new AddExpenseDialog("dialog");
         add(dialog);
         WebMarkupContainer wmc = new WebMarkupContainer("open");
         dialog.addOpenerAttributesTo(wmc);
         add(wmc);
 
         DataTable<Expense, String> table = createTable(getPageParameters());
 
         form = new StatelessForm<>(FORM);
         form.add(table);

        add(new StatelessPagingNavigator(PAGINATION, getPageParameters(), table)
         {
             @Override
             protected void onConfigure()
             {
                 setVisibilityAllowed(getPageable().getPageCount() > 1);
             }
         });
 
         add(new SubmitLink(DELETE, form)
         {
             @Override
             public void onSubmit()
             {
                 for (IModel<Expense> model : selectColumn.getSelection())
                 {
                     expenseService.delete(model.getObject());
                 }
 
                 setResponsePage(getPageClass(), getPageParameters());
             }
         });
 
         int total = expenseService.totalExpenses();
         long count = expenseService.count();
         add(new Label("total", total));
         add(new Label("count", count));
         add(new Label("average", count == 0 ? 0 : total / (double) count));
         add(new Label("weekly", expenseService.averageWeeklyExpenses()));
     }
 
     @Override
     protected void onInitialize()
     {
         super.onInitialize();
 
         add(form);
     }
 
     private DataTable<Expense, String> createTable(PageParameters parameters)
     {
         List<IColumn<Expense, String>> columns = Arrays.<IColumn<Expense, String>>asList(
                 selectColumn,
                 new PropertyColumn<Expense, String>(new ResourceModel("date"), "date", "date"),
                 new PropertyColumn<Expense, String>(new ResourceModel("description"), "description", "description"),
                 new PropertyColumn<Expense, String>(new ResourceModel("category"), "category", "category"),
                 new PropertyColumn<Expense, String>(new ResourceModel("cost"), "cost", "cost")
         );
         DomainProvider<Expense, Long> provider = new DomainProvider<>(expenseService);
         DataTable<Expense, String> table = new DataTable<>(TABLE, columns, provider, ROWS_PER_PAGE);
 
         table.addTopToolbar(new SortableHeadersToolbar<>(table, provider, parameters));
         table.addBottomToolbar(new NoRecordsToolbar(table));
 
         return table;
     }
 }
