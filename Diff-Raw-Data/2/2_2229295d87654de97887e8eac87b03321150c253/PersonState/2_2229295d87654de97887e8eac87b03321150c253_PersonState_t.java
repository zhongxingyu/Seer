 package net.sf.laja.cdd.state;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import net.sf.laja.cdd.ImmutableState;
 import net.sf.laja.cdd.InvalidStateException;
 import net.sf.laja.cdd.MutableState;
 import net.sf.laja.cdd.ValidationErrors;
 import net.sf.laja.cdd.annotation.Id;
 import net.sf.laja.cdd.annotation.Key;
 import net.sf.laja.cdd.annotation.Optional;
 import net.sf.laja.cdd.annotation.State;
 import net.sf.laja.cdd.validator.Validator;
 import org.joda.time.DateMidnight;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static net.sf.laja.cdd.ValidationErrors.concatenate;
 import static net.sf.laja.cdd.state.AddressState.AddressMutableState;
 import static net.sf.laja.cdd.stateconverter.TypeConversion.*;
 import static net.sf.laja.cdd.stateconverter.TypeConverters.*;
 import static net.sf.laja.cdd.validator.Validators.collectionValidator;
 import static net.sf.laja.cdd.validator.Validators.mapValidator;
 
 @State
 public class PersonState implements ImmutableState {
     @Key public final String name;
     @Id
     public final DateMidnight birthday;
     public final ImmutableList<PersonState> children;
     public final AddressState address;
     @Optional(defaultValue = "new LinkedHashSet<AddressMutableState>()")
     public final ImmutableSet<AddressState> oldAddresses;
     @Optional
     public final ImmutableMap<String,AddressState> groupedAddresses;
     public final ImmutableList<ImmutableSet<ImmutableMap<String,Integer>>> listOfSetOfMapOfIntegers;
 
     private static void validate(PersonMutableState state, Object rootElement, String parent, ValidationErrors.Builder errors) {
         if (state.birthday != null && state.birthday.isAfterNow()) {
            errors.addError(rootElement, parent, BIRTHDAY, "born_after_today");
         }
     }
 
     // ===== Generated code =====
 
     public static final String NAME = "name";
     public static final String BIRTHDAY = "birthday";
     public static final String CHILDREN = "children";
     public static final String ADDRESS = "address";
     public static final String OLD_ADDRESSES = "oldAddresses";
     public static final String GROUPED_ADDRESSES = "groupedAddresses";
     public static final String LIST_OF_SET_OF_MAP_OF_INTEGERS = "listOfSetOfMapOfIntegers";
 
     public PersonState(
             String name,
             DateMidnight birthday,
             ImmutableList<PersonState> children,
             AddressState address,
             ImmutableSet<AddressState> oldAddresses,
             ImmutableMap<String,AddressState> groupedAddresses,
             ImmutableList<ImmutableSet<ImmutableMap<String,Integer>>> listOfSetOfMapOfIntegers) {
         this.name = name;
         this.birthday = birthday;
         this.children = children;
         this.address = address;
         this.oldAddresses = oldAddresses;
         this.groupedAddresses = groupedAddresses;
         this.listOfSetOfMapOfIntegers = listOfSetOfMapOfIntegers;
     }
 
     public static class IllegalPersonStateException extends InvalidStateException {
         public IllegalPersonStateException(ValidationErrors errors) {
             super(errors);
         }
     }
 
     public String getName() { return name; }
     public DateMidnight getBirthday() { return birthday; }
     public ImmutableList<PersonState> getChildren() { return children; }
     public AddressState getAddress() { return address; }
     public ImmutableSet<AddressState> getOldAddresses() { return oldAddresses; }
     public ImmutableMap<String,AddressState> getGroupedAddresses() { return groupedAddresses; }
     public ImmutableList<ImmutableSet<ImmutableMap<String,Integer>>> getListOfSetOfMapOfIntegers() { return listOfSetOfMapOfIntegers; }
 
     public PersonState withName(String name) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
     public PersonState withBirthday(DateMidnight birthday) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
     public PersonState withChildren(ImmutableList<PersonState> children) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
     public PersonState withAddress(AddressState address) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
     public PersonState withOldAddresses(ImmutableSet<AddressState> oldAddresses) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
     public PersonState withGroupedAddresses(ImmutableMap<String,AddressState> groupedAddresses) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
     public PersonState withListOfSetOfMapOfIntegers(ImmutableList<ImmutableSet<ImmutableMap<String,Integer>>> listOfSetOfMapOfIntegers) { return new PersonState(name, birthday, children, address, oldAddresses, groupedAddresses, listOfSetOfMapOfIntegers); }
 
     public PersonMutableState asMutable() {
         return new PersonMutableState(
             name,
             birthday,
             asMutableList(children, toMutable),
             address.asMutable(),
             asMutableSet(oldAddresses, toMutable),
             asMutableMap(groupedAddresses, toMutable),
             asMutableList(listOfSetOfMapOfIntegers, toMutableSet, toMutableMap));
     }
 
     @Override
     public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
 
         return result;
     }
 
     @Override
     public boolean equals(Object that) {
         if (this == that) return true;
         if (that == null || getClass() != that.getClass()) return false;
 
         PersonState state = (PersonState)that;
 
         if (name != null ? !name.equals(state.name) : state.name != null) return false;
 
         return true;
     }
 
     public int hashCodeValue() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
         result = 31 * result + (children != null ? children.hashCode() : 0);
         result = 31 * result + (address != null ? address.hashCode() : 0);
         result = 31 * result + (oldAddresses != null ? oldAddresses.hashCode() : 0);
         result = 31 * result + (groupedAddresses != null ? groupedAddresses.hashCode() : 0);
         result = 31 * result + (listOfSetOfMapOfIntegers != null ? listOfSetOfMapOfIntegers.hashCode() : 0);
 
         return result;
     }
 
     public boolean equalsValue(Object that) {
         if (this == that) return true;
         if (that == null || getClass() != that.getClass()) return false;
 
         PersonState state = (PersonState)that;
 
         if (name != null ? !name.equals(state.name) : state.name != null) return false;
         if (birthday != null ? !birthday.equals(state.birthday) : state.birthday != null) return false;
         if (children != null ? !children.equals(state.children) : state.children != null) return false;
         if (address != null ? !address.equals(state.address) : state.address != null) return false;
         if (oldAddresses != null ? !oldAddresses.equals(state.oldAddresses) : state.oldAddresses != null) return false;
         if (groupedAddresses != null ? !groupedAddresses.equals(state.groupedAddresses) : state.groupedAddresses != null) return false;
         if (listOfSetOfMapOfIntegers != null ? !listOfSetOfMapOfIntegers.equals(state.listOfSetOfMapOfIntegers) : state.listOfSetOfMapOfIntegers != null) return false;
 
         return true;
     }
 
     @Override
     public String toString() {
         return "{name=" + (name == null ? null : '\'' + name + '\'' ) +
                 ", birthday=" + birthday +
                 ", children=" + children +
                 ", address=" + address +
                 ", oldAddresses=" + oldAddresses +
                 ", groupedAddresses=" + groupedAddresses +
                 ", listOfSetOfMapOfIntegers=" + listOfSetOfMapOfIntegers + '}';
     }
 
     @State(type = "mutable")
     public static class PersonMutableState implements MutableState {
         @Key public String name;
         @Id
         public DateMidnight birthday;
         public List<PersonMutableState> children;
         public AddressMutableState address;
         @Optional(defaultValue = "new LinkedHashSet<AddressMutableState>()")
         public Set<AddressMutableState> oldAddresses;
         @Optional
         public Map<String,AddressMutableState> groupedAddresses;
         public List<Set<Map<String,Integer>>> listOfSetOfMapOfIntegers;
 
         public PersonMutableState() {
             children = new ArrayList<PersonMutableState>();
             address = new AddressMutableState();
             oldAddresses = new LinkedHashSet<AddressMutableState>();
             groupedAddresses = new HashMap<String,AddressMutableState>();
             listOfSetOfMapOfIntegers = new ArrayList<Set<Map<String,Integer>>>();
         }
 
         public PersonMutableState(
                 String name,
                 DateMidnight birthday,
                 List<PersonMutableState> children,
                 AddressMutableState address,
                 Set<AddressMutableState> oldAddresses,
                 Map<String,AddressMutableState> groupedAddresses,
                 List<Set<Map<String,Integer>>> listOfSetOfMapOfIntegers) {
             this.name = name;
             this.birthday = birthday;
             this.children = children;
             this.address = address;
             this.oldAddresses = oldAddresses;
             this.groupedAddresses = groupedAddresses;
             this.listOfSetOfMapOfIntegers = listOfSetOfMapOfIntegers;
         }
 
         public String getName() { return name; }
         public DateMidnight getBirthday() { return birthday; }
         public List<PersonMutableState> getChildren() { return children; }
         public AddressMutableState getAddress() { return address; }
         public Set<AddressMutableState> getOldAddresses() { return oldAddresses; }
         public Map<String,AddressMutableState> getGroupedAddresses() { return groupedAddresses; }
         public List<Set<Map<String,Integer>>> getListOfSetOfMapOfIntegers() { return listOfSetOfMapOfIntegers; }
 
         public void setName(String name) { this.name = name; }
         public void setBirthday(DateMidnight birthday) { this.birthday = birthday; }
         public void setChildren(List<PersonMutableState> children) { this.children = children; }
         public void setAddress(AddressMutableState address) { this.address = address; }
         public void setOldAddresses(Set<AddressMutableState> oldAddresses) { this.oldAddresses = oldAddresses; }
         public void setGroupedAddresses(Map<String,AddressMutableState> groupedAddresses) { this.groupedAddresses = groupedAddresses; }
         public void setListOfSetOfMapOfIntegers(List<Set<Map<String,Integer>>> listOfSetOfMapOfIntegers) { this.listOfSetOfMapOfIntegers = listOfSetOfMapOfIntegers; }
 
         public void assertIsValid(Validator... validators) {
             ValidationErrors errors = validate(validators);
 
             if (errors.hasErrors()) {
                 throw new IllegalPersonStateException(errors);
             }
         }
 
         public boolean isValid() {
             return validate().isEmpty();
         }
 
         public ValidationErrors validate(Validator... validators) {
             ValidationErrors.Builder errors = ValidationErrors.builder();
             validate(this, "", errors, validators);
             return errors.build();
         }
 
         public void validate(Object rootElement, String parent, ValidationErrors.Builder errors, Validator... validators) {
             if (name == null) { errors.addIsNullError(rootElement, parent, "name"); }
             if (birthday == null) { errors.addIsNullError(rootElement, parent, "birthday"); }
             if (children == null) { errors.addIsNullError(rootElement, parent, "children"); }
             if (address == null) { errors.addIsNullError(rootElement, parent, "address"); }
             if (listOfSetOfMapOfIntegers == null) { errors.addIsNullError(rootElement, parent, "listOfSetOfMapOfIntegers"); }
 
             collectionValidator().validate(rootElement, "children", parent, "children", errors, validators, 0);
             address.validate(rootElement, concatenate(parent, "address"), errors);
             collectionValidator().validate(rootElement, "oldAddresses", parent, "oldAddresses", errors, validators, 0);
             mapValidator().validate(rootElement, "groupedAddresses", parent, "groupedAddresses", errors, validators, 0);
             collectionValidator().validate(rootElement, "listOfSetOfMapOfIntegers", parent, "listOfSetOfMapOfIntegers", errors, validators, 0);
 
             PersonState.validate(this, rootElement, parent, errors);
 
             for (Validator validator : validators) {
                 validator.validate(rootElement, this, parent, "", errors);
             }
         }
 
         public PersonState asImmutable(Validator... validators) {
             assertIsValid(validators);
 
             return new PersonState(
                 name,
                 birthday,
                 asImmutableList(children, toImmutable),
                 address.asImmutable(),
                 asImmutableSet(oldAddresses, toImmutable),
                 asImmutableMap(groupedAddresses, toImmutable),
                 asImmutableList(listOfSetOfMapOfIntegers, toImmutableSet, toImmutableMap));
         }
 
         @Override
         public int hashCode() {
             int result = name != null ? name.hashCode() : 0;
 
             return result;
         }
 
         @Override
         public boolean equals(Object that) {
             if (this == that) return true;
             if (that == null || getClass() != that.getClass()) return false;
 
             PersonMutableState state = (PersonMutableState)that;
 
             if (name != null ? !name.equals(state.name) : state.name != null) return false;
 
             return true;
         }
 
         @Override
         public String toString() {
             return "{name=" + (name == null ? null : '\'' + name + '\'' ) +
                     ", birthday=" + birthday +
                     ", children=" + children +
                     ", address=" + address +
                     ", oldAddresses=" + oldAddresses +
                     ", groupedAddresses=" + groupedAddresses +
                     ", listOfSetOfMapOfIntegers=" + listOfSetOfMapOfIntegers + '}';
         }
     }
 }
