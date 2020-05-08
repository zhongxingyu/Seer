 package au.org.scoutmaster.views;
 
 import java.util.ArrayList;
 
 import au.com.vaadinutils.crud.BaseCrudView;
 import au.com.vaadinutils.crud.ChildCrudView;
 import au.com.vaadinutils.crud.FormHelper;
 import au.com.vaadinutils.crud.HeadingPropertySet;
 import au.com.vaadinutils.crud.HeadingPropertySet.Builder;
 import au.com.vaadinutils.crud.MultiColumnFormLayout;
 import au.com.vaadinutils.crud.ValidatingFieldGroup;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.dao.Path;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.Contact_;
 import au.org.scoutmaster.domain.Raffle;
 import au.org.scoutmaster.domain.RaffleAllocation_;
 import au.org.scoutmaster.domain.RaffleBook;
 import au.org.scoutmaster.domain.RaffleBook_;
 import au.org.scoutmaster.domain.Raffle_;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.Container.Filter;
 import com.vaadin.data.util.filter.Or;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.shared.ui.combobox.FilteringMode;
 import com.vaadin.shared.ui.datefield.Resolution;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 
 public class RaffleBookChildView extends ChildCrudView<Raffle, RaffleBook>
 {
 	private static final long serialVersionUID = 1L;
 
 	public RaffleBookChildView(BaseCrudView<Raffle> parentCrud)
 	{
 		super(parentCrud, Raffle.class, RaffleBook.class, Raffle_.id, RaffleBook_.raffle.getName());
 
 		JPAContainer<RaffleBook> container = new DaoFactory().getRaffleBookDao().createVaadinContainer();
 		container.sort(new String[]
 		{ RaffleBook_.firstNo.getName() }, new boolean[]
 		{ true });
 
 		Builder<RaffleBook> builder = new HeadingPropertySet.Builder<RaffleBook>();
 		builder.addColumn("First No.", RaffleBook_.firstNo)
 		.addColumn("Allocated To", new Path(RaffleBook_.raffleAllocation, RaffleAllocation_.allocatedTo).getName())
 		.addColumn("Date Allocated", new Path(RaffleBook_.raffleAllocation, RaffleAllocation_.dateAllocated).getName())
 		.addColumn("Date Returned", RaffleBook_.dateReturned);
 		super.init(RaffleBook.class, container, builder.build());
 
 	}
 
 	@Override
 	protected Component buildEditor(ValidatingFieldGroup<RaffleBook> fieldGroup2)
 	{
 		MultiColumnFormLayout<RaffleBook> overviewForm = new MultiColumnFormLayout<RaffleBook>(1, this.fieldGroup);
 		overviewForm.setColumnFieldWidth(0, 240);
 		overviewForm.setColumnLabelWidth(0, 110);
 		// overviewForm.setColumnExpandRatio(0, 1.0f);
 		overviewForm.setSizeFull();
 
 		
 		FormHelper<RaffleBook> formHelper = overviewForm.getFormHelper();
 		
 		overviewForm.bindTextField("Ticket Count", RaffleBook_.ticketCount);
 		overviewForm.bindTextField("First No.", RaffleBook_.firstNo);
 
 		ComboBox allocatedTo = formHelper.new EntityFieldBuilder<Contact>()
				.setLabel("Allocated By")
 				.setField(new Path(RaffleBook_.raffleAllocation, RaffleAllocation_.allocatedTo).getName())
 				.setListFieldName(Contact_.fullname)
 				.setListClass(Contact.class)
 				.build();
 		allocatedTo.setFilteringMode(FilteringMode.CONTAINS);
 		allocatedTo.setTextInputAllowed(true);
 		allocatedTo.setNullSelectionAllowed(true);
 		allocatedTo.setDescription("The person the book has been allocated to.");
 
 
 		overviewForm.bindTextField("Tickets Returned?", RaffleBook_.ticketsReturned)
 			.setDescription("The no. of tickets that have been returned.");
 		
 		overviewForm.bindTextField("Amount Returned?", RaffleBook_.amountReturned)
 		.setDescription("The amount of money returned for this book.");
 		
 		overviewForm.bindDateField("Date Returned", RaffleBook_.dateReturned, "yyyy-MM-dd", Resolution.DAY)
 			.setDescription("The date the money and tickets for this book were returned");
 
 		ComboBox collectedBy = formHelper.new EntityFieldBuilder<Contact>()
 				.setLabel("Issued By").setField(RaffleBook_.collectedBy).setListFieldName(Contact_.fullname).build();
 		collectedBy.setFilteringMode(FilteringMode.CONTAINS);
 		collectedBy.setTextInputAllowed(true);
 		collectedBy.setDescription("The leader that collected the ticket stubs and money.");
 
 		overviewForm.bindBooleanField("Receipt Issued?", RaffleBook_.receiptIssued)
 		.setDescription("Has a receipt been issued for the return of this book?");
 		
 		
 		overviewForm.bindTextAreaField("Notes", RaffleBook_.notes, 6);
 	
 		
 		return overviewForm;
 	}
 
 	@Override
 	protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
 	{
 		return new FilterBuilder()
 		.or(new SimpleStringFilter(RaffleBook_.firstNo.getName(), filterString, true,false))
 		.or(new SimpleStringFilter(new Path(RaffleBook_.raffleAllocation, RaffleAllocation_.allocatedTo).getName(), filterString, true, false))
 		.or(new SimpleStringFilter(new Path(RaffleBook_.raffleAllocation, RaffleAllocation_.dateAllocated).getName(), filterString, true, false))
 		.build();
 	}
 
 	@Override
 	public void associateChild(Raffle newParent, RaffleBook child)
 	{
 		newParent.addRaffleBook(child);
 	}
 	
 	static class FilterBuilder
 	{
 		ArrayList<Filter> filters = new ArrayList<>();
 		
 		FilterBuilder or(Filter filter)
 		{
 			filters.add(filter);
 			
 			return this;
 		}
 		
 		Or build()
 		{
 			Filter[] aFilters = new Filter[1];
 			return new Or(filters.toArray(aFilters));
 		}
 	}
 }
