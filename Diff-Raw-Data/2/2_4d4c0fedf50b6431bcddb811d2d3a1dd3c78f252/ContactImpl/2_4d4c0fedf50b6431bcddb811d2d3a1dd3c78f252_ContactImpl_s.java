 import java.io.Serializable;
 /**
 * A contact is a person we are making business with or may do in the future.
 *
 * Contacts have an ID (unique), a name (probably unique, but maybe
 * not), and notes that the user may want to save about them.
 */
 public class ContactImpl implements Contact, Serializable {
     private static int idStatic = 0;
     private int id;
     private StringBuilder name;
     private StringBuilder notes;
 	
     /**
     * Constructor with no parameters to make class serialisable
     */
     public ContactImpl() {}
     /**
     * Constructor for contact
     *
     * @param name of contact
     * @param newNote for contact
     */
     public ContactImpl(String name, String newNote) {
         //Prevents more than one contact having the same id
         id = idStatic++;
		this.name = new StringBuilder(name);
         this.notes = new StringBuilder(newNote + "\n");
     }
     /**
     * Constructor that only takes name
     *
     * @param name of contact
     */
     public ContactImpl(String name) {
         this(name, "");
     }
     /**
     * Returns the ID of the contact.
     *
     * @return the ID of the contact.
     */
     public int getId() {
         return id;
     }
     /**
     * Returns the name of the contact.
     *
     * @return the name of the contact.
     */
     public String getName() {
         return name.toString();
     }
     /**
     * Returns our notes about the contact, if any.
     *
     * If we have not written anything about the contact, the empty
     * string is returned.
     *
     * @return a string with notes about the contact, maybe empty.
     */
     public String getNotes() {
         return notes.toString();
     }
     /**
     * Add notes about the contact.
     *
     * @param note the notes to be added
     */
     public void addNotes(String newNote) {
         notes.append(newNote + "\n");
     }		
 }
