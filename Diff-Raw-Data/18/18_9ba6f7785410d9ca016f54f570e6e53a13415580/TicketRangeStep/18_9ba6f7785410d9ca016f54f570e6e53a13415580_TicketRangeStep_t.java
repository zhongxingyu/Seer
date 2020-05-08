 package au.org.scoutmaster.views.wizards.raffle.importBooks;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.vaadin.teemu.wizards.WizardStep;
 
 import au.com.vaadinutils.crud.MultiColumnFormLayout;
 import au.com.vaadinutils.fields.FieldValidator;
 import au.org.scoutmaster.domain.Raffle;
 import au.org.scoutmaster.domain.RaffleBook;
 import au.org.scoutmaster.util.SMNotification;
 
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.fieldgroup.FieldGroup;
 import com.vaadin.data.util.converter.StringToIntegerConverter;
 import com.vaadin.data.validator.IntegerRangeValidator;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 public class TicketRangeStep implements WizardStep
 {
 	@SuppressWarnings("unused")
 	private static Logger logger = LogManager.getLogger(TicketRangeStep.class);
 	private RaffleBookImportWizardView wizard;
 	private TextField firstTicketNoField;
 	private TextField noOfBooksField;
 	private TextField lastTicketNoField;
 	private MultiColumnFormLayout<Integer> formLayout;
 
 	FieldGroup fieldGroup = new FieldGroup();
 	private FieldValidator fieldValidator;
 
 	public TicketRangeStep(RaffleBookImportWizardView setupWizardView)
 	{
 		this.wizard = setupWizardView;
 	}
 
 	@Override
 	public String getCaption()
 	{
 		return "Ticket Range";
 	}
 
 	@Override
 	public Component getContent()
 	{
 		VerticalLayout layout = new VerticalLayout();
 		layout.setMargin(true);
 
 		fieldValidator = new FieldValidator();
 		formLayout = new MultiColumnFormLayout<Integer>(1, null); // new
 																	// ValidatingFieldGroup(dynamicFieldItem));
 		formLayout.setColumnLabelWidth(0, 150);
 		formLayout.setColumnFieldWidth(0, 250);
 		formLayout.setSizeFull();
 
 		Label label = new Label("<h1>Enter the details for the group of books you want to import.</h1>",
 				ContentMode.HTML);
 
 		layout.addComponent(label);
 		layout.addComponent(formLayout);
 
 		firstTicketNoField = formLayout.addTextField("First Book Ticket No.");
 		noOfBooksField = formLayout.addTextField("No. of consecutive Books");
 		noOfBooksField.setDescription("The no of Books to be imported. They must be in a consecutive number range!");
 		lastTicketNoField = formLayout.addTextField("Last Ticket No. of Last Book");
 		lastTicketNoField
 				.setDescription("Enter the ticket no of the 'last' ticket of the 'last' book. This is used to check that all of the details are correct.");
 
 		firstTicketNoField.addValidator(new IntegerRangeValidator("First Ticket No must be an integer", 0, 6000000));
 		firstTicketNoField.setConverter(new StringToIntegerConverter());
 		firstTicketNoField.setRequired(true);
 		fieldValidator.addField(firstTicketNoField);
 
 		noOfBooksField.addValidator(new IntegerRangeValidator("No. of Books must be an integer", 1, 1000));
 		noOfBooksField.setConverter(new StringToIntegerConverter());
 		noOfBooksField.setRequired(true);
 		fieldValidator.addField(noOfBooksField);
 
		lastTicketNoField.addValidator(new IntegerRangeValidator("Last Ticket No. must be an integer", 1, 6000000));
 		lastTicketNoField.setConverter(new StringToIntegerConverter());
 		lastTicketNoField.setRequired(true);
 		fieldValidator.addField(lastTicketNoField);
 
 		Label labelImport = new Label("<h1>Clicking Next will import the books!</h1>", ContentMode.HTML);
 
 		layout.addComponent(labelImport);
 
 		return layout;
 	}
 
 	@Override
 	public boolean onAdvance()
 	{
 		boolean advance = false;
 		Raffle raffle = wizard.getRaffle();
 
 		try
 		{
 			fieldValidator.validate();
 
			int firstTicketNo = (Integer)firstTicketNoField.getConvertedValue();
			int lastTicketNo = (Integer)lastTicketNoField.getConvertedValue();
			int noOfBooks = (Integer)noOfBooksField.getConvertedValue();
 			int ticketsPerBook = raffle.getTicketsPerBook();
 
 			RaffleBook book = findFirstDuplicateBook(raffle, firstTicketNo, lastTicketNo);
 
 			if (book != null)
 			{
 				SMNotification.show("Please check book no.s as the book: " + book.toString()
 						+ " has already been imported.", Type.WARNING_MESSAGE);
 
 			}
 			else
 			{
 				int calcLastTicket = firstTicketNo + (noOfBooks * ticketsPerBook) - 1;
 				if (calcLastTicket != lastTicketNo)
 				{
 					int expectedLast = firstTicketNo + (noOfBooks * ticketsPerBook) - 1;
 					SMNotification
 							.show("Please check the first and last ticket no. and the no. of books as the values you entered don't add up. We expected the last ticket to be: "
 									+ expectedLast, Type.WARNING_MESSAGE);
 				}
 				else
 					advance = true;
 			}
 		}
 		catch (InvalidValueException e)
 		{
 			SMNotification.show(e, Type.ERROR_MESSAGE);
 		}
 
 		return advance;
 	}
 
 	private RaffleBook findFirstDuplicateBook(Raffle raffle, int firstTicketNo, int lastTicketNo)
 	{
 		RaffleBook first = null;
 		Set<RaffleBook> books = raffle.getImportedBooks();
 		RaffleBook[] sortedBooks =  books.toArray(new RaffleBook[]{});
 		Arrays.sort(sortedBooks, new Comparator<RaffleBook>()
 		{
 
 			@Override
 			public int compare(RaffleBook o1, RaffleBook o2)
 			{
 				return o1.getFirstNo().intValue() - o2.getFirstNo().intValue();
 			}
 		});
 
 		for (RaffleBook book : sortedBooks)
 		{
 			int firstTicket = book.getFirstNo().intValue();
 			int lastTicket = book.getFirstNo() + book.getTicketCount() - 1;
 
 			if (firstTicket >= firstTicketNo && firstTicket <= lastTicketNo
 					|| (lastTicket >= firstTicketNo && lastTicket <= lastTicketNo))
 			{
 				first = book;
 				break;
 			}
 		}
 		return first;
 	}
 
 	@Override
 	public boolean onBack()
 	{
 		return true;
 	}
 
 	public int getFirstBookNo()
 	{
		return ((Integer)firstTicketNoField.getConvertedValue()).intValue();
 	}
 
 	public int getLastBookNo()
 	{
		return ((Integer)lastTicketNoField.getConvertedValue()).intValue();
 	}
 
 	public int getBookCount()
 	{
		return ((Integer)noOfBooksField.getConvertedValue()).intValue();
 	}
 
 }
