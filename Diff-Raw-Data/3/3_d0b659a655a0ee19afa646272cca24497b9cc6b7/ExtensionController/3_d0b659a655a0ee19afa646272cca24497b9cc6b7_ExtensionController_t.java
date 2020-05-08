 package at.owlsoft.owl.usecases;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import at.owlsoft.owl.business.ControllerBase;
 import at.owlsoft.owl.business.OwlApplicationContext;
 import at.owlsoft.owl.dao.DaoManager;
 import at.owlsoft.owl.model.IDefaultRoles;
 import at.owlsoft.owl.model.NoPermissionException;
 import at.owlsoft.owl.model.SearchField;
 import at.owlsoft.owl.model.SearchFieldType;
 import at.owlsoft.owl.model.accounting.Activity;
 import at.owlsoft.owl.model.accounting.ActivityStatus;
 import at.owlsoft.owl.model.accounting.FilingExtension;
 import at.owlsoft.owl.model.accounting.Rental;
 import at.owlsoft.owl.model.accounting.Reservation;
 import at.owlsoft.owl.model.media.MediumExemplar;
 import at.owlsoft.owl.model.media.MediumExemplarStatus;
 import at.owlsoft.owl.model.user.SystemUserStatus;
 import at.owlsoft.owl.validation.ValidationMessage;
 import at.owlsoft.owl.validation.ValidationMessageStatus;
 
 public class ExtensionController extends ControllerBase
 {
 
     private static final int DEFAULT_MAX_EXTENSIONS     = 3;
     private static final int DEFAULT_EXTENSION_DURATION = 7;
 
     public ExtensionController(OwlApplicationContext context)
     {
         super(context);
     }
 
     private List<ValidationMessage> _messages;
 
     public List<ValidationMessage> extend(MediumExemplar copy)
             throws NoPermissionException
     {
         getContext().getAuthenticationController().checkAccess(
                 IDefaultRoles.RENTAL_EXTEND);
 
         Rental rental = copy.getLastRental();
 
         if (validate(rental))
         {
 
             FilingExtension fex = new FilingExtension();
             fex.setCreationDate(new Date());
 
             Date endDate = new Date(rental.getEndDate().getTime()
                     + getExtensionPeriode(rental.getMediumExemplar()));
             fex.setNewEndDate(endDate);
             fex.setRental(rental);
 
             rental.addFilingExtension(fex);
 
             DaoManager.getInstance().getRentalDao().store(rental);
         }
 
         return _messages;
 
     }
 
     private long getExtensionPeriode(MediumExemplar copy)
     {
         // TODO Read extension periode from config.
         Class<?> c = copy.getClass();
         String name = c.getName().concat("extensionDuration");
         return getContext().getConfigurationController().getInt(name,
                 DEFAULT_EXTENSION_DURATION)
                 * 24 * 60 * 60 * 1000;
     }
 
     private boolean validate(Rental rental)
     {
 
         boolean hasNoError = true;
 
         _messages = new ArrayList<ValidationMessage>();
         // TODO Read from config maximum FilingExtension number
         Class<?> c = rental.getMediumExemplar().getClass();
         String name = c.getName().concat("maxExtensions");
         int maxExtensions = getContext().getConfigurationController().getInt(
                 name, DEFAULT_MAX_EXTENSIONS);
 
         if (rental.getFilingExtensionCount() >= maxExtensions)
         {
             _messages.add(new ValidationMessage("Maximum extensions reached.",
                     ValidationMessageStatus.Error));
             hasNoError = false;
         }
 
         if (rental.getCustomer().getLastSystemUserStatusEntry()
                 .getSystemUserStatus() != SystemUserStatus.Active)
         {
             _messages.add(new ValidationMessage("Customer not active.",
                     ValidationMessageStatus.Warning));
         }
 
         // check for open reservations
         List<Activity> activities = rental.getMedium().getActivities();
         int reservationCount = 0;
         int rentableCopies = 0;
         for (Activity activity : activities)
         {
             if (activity instanceof Reservation)
             {
                 Reservation reservation = (Reservation) activity;
                 if (reservation.getCurrentStatus().equals(ActivityStatus.Open))
                 {
                     reservationCount++;
                 }
 
             }
         }
         // count rentable copies
         List<MediumExemplar> copies = rental.getMedium().getMediumExemplars();
         for (MediumExemplar copy : copies)
         {
             if (copy.getCurrentState().equals(MediumExemplarStatus.Rentable))
             {
                 rentableCopies++;
             }
         }
        // check wether there are more reservations than rentable copies
        if (reservationCount >= rentableCopies)
         {
             _messages.add(new ValidationMessage(
                     "Not enough rentable copies for reservations.",
                     ValidationMessageStatus.Warning));
         }
         return hasNoError;
 
     }
 
     public List<ValidationMessage> extend(UUID uuid)
             throws NoPermissionException
     {
         getContext().getAuthenticationController().checkAccess(
                 IDefaultRoles.RENTAL_EXTEND);
 
         List<ValidationMessage> temp = new ArrayList<ValidationMessage>();
 
         List<SearchField> searchFields = new ArrayList<SearchField>();
         searchFields.add(new SearchField("_UUID", uuid.toString(),
                 SearchFieldType.Equals));
 
         List<Rental> rentals = getContext().getRentalSearchController().search(
                 searchFields);
 
         if (rentals == null || rentals.isEmpty())
         {
             temp.add(new ValidationMessage("No rental found.",
                     ValidationMessageStatus.Error));
         }
         else if (rentals.size() > 1)
         {
             temp.add(new ValidationMessage("To many rentals found.",
                     ValidationMessageStatus.Error));
         }
         else
         {
             temp = extend(rentals.get(0).getMediumExemplar());
         }
 
         return temp;
 
     }
 
 }
