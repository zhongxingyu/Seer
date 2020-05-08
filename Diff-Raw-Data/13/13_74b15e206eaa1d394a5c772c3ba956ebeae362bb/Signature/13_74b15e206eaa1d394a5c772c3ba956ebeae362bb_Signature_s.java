 package de.skuzzle.polly.sdk;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import de.skuzzle.polly.sdk.Types.AnyType;
 import de.skuzzle.polly.sdk.Types.BooleanType;
 import de.skuzzle.polly.sdk.Types.ChannelType;
 import de.skuzzle.polly.sdk.Types.CommandType;
 import de.skuzzle.polly.sdk.Types.DateType;
 import de.skuzzle.polly.sdk.Types.ListType;
 import de.skuzzle.polly.sdk.Types.NumberType;
 import de.skuzzle.polly.sdk.Types.StringType;
 import de.skuzzle.polly.sdk.Types.UserType;
 
 
 
 /**
  * <p>This class represents a signature for a {@link Command}. It is used for formal and 
  * actual signatures. That means, any Command may register a set of different formal 
  * signatures that they are compatible with.</p> 
  * 
  * <p>If the command is executed, each formal signature is compared to the actual 
  * signature that was entered by the user to resolve which action the command shall 
  * execute.</p>
  * 
  * <p>The resolved signature gets passed to the 
  * {@link Command#executeOnChannel(User, String, Signature)} or
  * {@link Command#executeOnQuery(User, Signature)} method. The {@link Command} can 
  * distinguish the actual passed signatures by their id. The formal signatures
  * are numbered consecutively in order of their creation beginning by 0.</p>
  *  
  * @author Simon
  * @since zero day
  * @version RC 1.0
  */
 public class Signature {
 
 	/**
 	 * The actual or formal parameters for this signature.
 	 */
 	private List<Types> parameters;
 	
 	/**
 	 * The command name that this signatures is for.
 	 */
 	private String name;
 	
 	/**
 	 * The id of this signature.
 	 */
 	private int signatureId;
 	
 	/**
 	 * This attributes is set to true if this signature only contains distinct
 	 * types.
 	 */
 	private boolean canonical;
 
 	
 	
 	/**
 	 * Creates a new signature with no required permissions.
 	 * 
 	 * @param name The name of the command that this signature is for. The name must 
 	 * 		exactly equal the commands name.
 	 * @param id The id of this signature. Commands distinguish actual signatures by 
 	 * 		this id.
 	 * @param parameters The parameters of this signature. If this is a formal signature,
 	 * 		the values of the parameter types are ignored. This parameter can be empty.
 	 */
 	public Signature(String name, int id, Types...parameters) {
 		this(name, id, Arrays.asList(parameters));
 	}
 	
 	
 	
 	/**
 	 * Creates a new signature.
 	 * 
 	 * @param name The name of the command that this signature is for. The name must 
 	 * 		exactly equal the commands name.
 	 * @param id The id of this signature. Commands distinguish actual signatures by 
 	 * 		this id.
 	 * @param parameters The parameters of this signature. If this is a formal signature,
 	 * 		the values of the parameter types are ignored. This list can be empty.
 	 * @param permissionName The name of the permission that is required in order to 
 	 *      execute this signature.
 	 */
 	public Signature(String name, int id, List<Types> parameters) {
 		this.name = name;
 		this.signatureId = id;
 		this.parameters = parameters;
 		this.canonical = this.checkCanonical(parameters);
 	}
 	
 	
 	
 	/**
 	 * Gets whether this signature is canonical, i.e. it only contains distinct
 	 * types.
 	 * 
 	 * @return <code>true</code> if this signature only contains distinct types
 	 * since 0.8
 	 */
 	public boolean isCanonical() {
 		return this.canonical;
 	}
 	
 	
 	
 	/**
 	 * Returns this signatures parameters.
 	 * 
 	 * @return The parameters.
 	 * @since 0.8
 	 */
     public List<Types> getParameters() {
         return this.parameters;
     }
     
 	
 	
 	/**
 	 * Returns the name of the command that this signature is for.
 	 * @return The commands name.
 	 */
 	public String getName() {
 		return this.name;
 	}
 	
 	
 	
 	/**
 	 * Returns the id of this signature.
 	 * @return The signature id.
 	 */
 	public int getId() {
 		return this.signatureId;
 	}
 	
 	
 	
 	/**
 	 * Sets the id for this signature. Its highly discouraged to use this method. It is 
 	 * used by {@link CommandManager#getCommand(Signature)} to set the formal id
 	 * to the actual signature if it was resolved.
 	 * 
 	 * @param id The new id for this signature.
 	 */
 	public void setId(int id) {
 		this.signatureId = id;
 	}
 	
 	
 	
 	/**
 	 * Returns a parameter of this signature. You can access the actual values of the 
 	 * signature via this method. You must cast the returnvalue to your expected
 	 * {@link Types} instance.
 	 * 
 	 * @param paramId The index of the parameter to retrieve.
 	 * @return The {@link Types} instance at the given index.
 	 */
 	public Types getValue(int paramId) {
 		return this.parameters.get(paramId);
 		
 	}
 	
 	
 	
 	/**
 	 * This is a convenience method for accessing a number parameter. Note that this
 	 * method will crash if the parameter with given id is no {@link NumberType}. 
 	 * 
 	 * @param paramId The parameter whose value shall be resolved.
 	 * @return The parameters number value.
 	 */
 	public double getNumberValue(int paramId) {
 		NumberType nt = (NumberType) this.parameters.get(paramId);
 		return nt.getValue();
 	}
 	
 	
 	
 	/**
 	 * This is a convenience method for accessing a data parameter. Note that this
 	 * method will crash if the parameter with given id is no {@link DateType}.
 	 * 
 	 * @param paramId The parameter whose value shall be resolved.
 	 * @return The parameters date value.
 	 */
 	public Date getDateValue(int paramId) {
 	    DateType dt = (DateType) this.parameters.get(paramId);
 	    return dt.getValue();
 	}
 	
 	
 	
 	/**
 	 * This is a convenience method for accessing a string parameter. It returns the
 	 * String values for any of these types: {@link StringType}, {@link UserType},
 	 * {@link CommandType} and {@link ChannelType}.
 	 * 
 	 * @param paramId The parameter whose value shall be resolved.
 	 * @return The parameters string value.
 	 * @throws IllegalArgumentException If the parameter is none of the above mentioned
 	 * 		types.
 	 */
 	public String getStringValue(int paramId) {
 		Types t = this.parameters.get(paramId);
 		if (t instanceof StringType) {
 			return ((StringType) t).getValue();
 		} else if (t instanceof UserType) {
 			return ((UserType) t).getValue();
 		} else if (t instanceof ChannelType) {
 			return ((ChannelType) t).getValue();
 		} else if (t instanceof CommandType) {
 			return ((CommandType) t).getValue();
 		}
 		throw new IllegalArgumentException("No String-type parameter");
 	}
 	
 	
 	
 	/**
 	 * This is a convenience method for accessing a boolean parameter. Note that this
 	 * method will crash if the parameter with given id is no {@link BooleanType}.
 	 * 
      * @param paramId The parameter whose value shall be resolved.
      * @return The parameters boolean value.
      * @since Beta 0.2
 	 */
 	public boolean getBooleanValue(int paramId) {
 	    BooleanType bt = (BooleanType) this.parameters.get(paramId);
 	    return bt.getValue();
 	}
 	
 	
 	/**
 	 * This is a convenience method for accessing list parameters. It returns a list
 	 * with the ListTypes elements.
      *
 	 * @param subType The type of the elements that are contained in the list.
 	 * @param paramId The id of the list parameter. Note that calling this method
 	 *     for a parameter which is no ListType will result in a 
 	 *     {@link ClassCastException}.
 	 * @return A new List with correct generic type, containing the ListTypes elements.
 	 */
 	public <T extends Types> List<T> getListValue(Class<T> subType, int paramId) {
 		ListType lt = (ListType) this.parameters.get(paramId);
 		List<T> result = new ArrayList<T>(lt.getElements().size());
 		for (Types t : lt.getElements()) {
 		    result.add(subType.cast(t));
 		}
 
 		return result;
 	}
 	
 	
 	
 	
 	/**
 	 * Matches two signatures against each others. They match if their parameter
 	 * types equal pairwise.
 	 * 
 	 * If both, this and the other signature are canonical as determined by 
 	 * {@link #isCanonical()}, this method first tries to rearrange the parameters 
 	 * of the other signature to match the sequence of this signature.
 	 * 
 	 * @param other The signature to match against this signature.
 	 * @return This signature if the signatures matches or <code>null</code> if they
 	 * 		do not match.
 	 */
 	public Signature match(Signature other) {
 		if (other.parameters.size() != this.parameters.size()) {
 			return null;
 		}
 		if (this.isCanonical() && other.isCanonical()) {
 			// if both are canonical, we try to rearrange it to increase
 			// chance of matching.
 			// Additional, its of high importance that the parameter
 			// sequence of both signatures match if the result is
 			// positive
 			this.rearrange(other);
 		}
 		
 		
 		
 		Iterator<Types> formal = this.parameters.iterator();
 		Iterator<Types> actual = other.parameters.iterator();
 		while (formal.hasNext()) {
 			if (!formal.next().check(actual.next())) {
 				return null;
 			}
 		}
 		return this;
 	}
 	
 	
 	
 	private void rearrange(Signature other) {
 		List<Types> formal = this.parameters;
 		List<Types> actual = other.parameters;
 		for (int i = 0; i < formal.size(); ++i) {
 			for (int j = i; j < actual.size(); ++j) {
			    Class<?> formalCls = formal.get(i).getClass();
			    Class<?> actualCls = actual.get(j).getClass();
				if(i != j && actualCls.isAssignableFrom(formalCls)) {
 					this.swap(actual, i, j);
 				}
 			}
 		}
 	}
 	
 	
 	
 	private void swap(List<Types> list, int i, int j) {
 		Types tmp = list.get(i);
 		list.set(i, list.get(j));
 		list.set(j, tmp);
 	}
 	
 	
 	
 	
 	/**
 	 * Checks whether the given parameter list is canonical.
 	 * 
 	 * @param parameters List of parameters to check.
 	 * @return If the list is canonical.
 	 */
 	private boolean checkCanonical(List<Types> parameters) {
         for (int i = 0; i < parameters.size(); ++i) {
             // signatures that contain the Any-Type can't be canonical
             if (parameters.get(i) instanceof AnyType) {
                 return false;
             }
             for (int j = i; j < parameters.size(); ++j) {
                Class<?> formalCls = parameters.get(i).getClass();
                Class<?> actualCls = parameters.get(j).getClass();
                if(i != j && actualCls.isAssignableFrom(formalCls)) {
                     return false;
                 }
             }
         }
 		return true;
 	}
 	
 	
 	
 	@Override
     public int hashCode() {
 	    // HACK: this method does not completely conform to equals() as equals tries to
 	    //       rearrange signatures to increase the possibility of a match and thus
 	    //       produces no canonical result
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.canonical ? 1231 : 1237);
         result = prime * result
             + ((this.parameters == null) ? 0 : this.parameters.hashCode());
         return result;
     }
 	
 	
 	
     /**
      * Two signatures are considered equals if they match each other.
      * 
      * @param obj The other signature to compare this one with.
      * @return <code>true</code> if the signatures match, <code>false</code> otherwise.
      * @see #match(Signature)
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         } else if (obj == this) {
             return true;
         } else if (!(obj instanceof Signature)) {
             return false;
         }
         
         Signature other = (Signature) obj;
         return this.match(other) != null;
     }
 
 
 
     @Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		b.append(":");
 		b.append(this.getName());
 		if (this.parameters.isEmpty()) {
 			return b.toString();
 		}
 		
 		b.append(" ");
 		Iterator<Types> it = this.parameters.iterator();
 		while (it.hasNext()) {
 			b.append(it.next().toString());
 			if (it.hasNext()) {
 				b.append(" ");
 			}
 		}
 		return b.toString();
 	}
 }
