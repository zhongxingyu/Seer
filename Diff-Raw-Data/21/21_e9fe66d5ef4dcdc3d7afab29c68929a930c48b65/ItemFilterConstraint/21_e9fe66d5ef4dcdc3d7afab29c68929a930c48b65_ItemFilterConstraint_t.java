 package edu.gatech.oad.rocket.findmythings.list;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import edu.gatech.oad.rocket.findmythings.model.DBItem;
 import edu.gatech.oad.rocket.findmythings.model.Category;
 
 import java.util.Date;
 
 /**
  * A constraint filter for items.
  * User: zw
  * Date: 4/16/13
  * Time: 7:31 PM
  */
 public class ItemFilterConstraint implements CustomFilter.Constraint<DBItem>, Parcelable {
 
 	public ItemFilterConstraint() {}
 
 	private Date dateAfter;
 	private Category category;
 	private Boolean open;
 
 	@Override
 	public boolean isEmpty() {
 		return dateAfter == null && category == null && open == null;
 	}
 
 	public void setDateAfter(Date dateAfter) {
 		this.dateAfter = dateAfter;
 	}
 
 	public Date getDateAfter() {
 		return dateAfter;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	public Boolean isOpen() {
 		return open;
 	}
 
 	public void setOpen(Boolean open) {
 		this.open = open;
 	}
 
 	public int describeContents() {
 		return 0;
 	}
 
 	public void writeToParcel(Parcel out, int flags) {
 		if (getDateAfter() != null) out.writeSerializable(getDateAfter());
 		int insanity = open == null ? 0 : (open ? 2: 1);
 		out.writeInt(insanity);
		if(category!=null)
 		out.writeString(category.toString());
 	}
 
 	public static final Parcelable.Creator<ItemFilterConstraint> CREATOR = new Parcelable.Creator<ItemFilterConstraint>() {
 		public ItemFilterConstraint createFromParcel(Parcel in) {
 			return new ItemFilterConstraint(in);
 		}
 
 		public ItemFilterConstraint[] newArray(int size) {
 			return new ItemFilterConstraint[size];
 		}
 	};
 
 	private ItemFilterConstraint(Parcel in) {
 		super();
 
 		this.dateAfter = (Date)in.readSerializable();
 
 		int insanity = in.readInt();
 		if (insanity == 0) this.open = null;
 		else this.open = (insanity == 2);
 
 		String catString = in.readString();
 		if (catString != null) this.category = Category.valueOf(in.readString());
 	}
 
 }
