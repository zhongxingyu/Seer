 package com.selesse.android.winedb.database;
 
 import android.content.ContentValues;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import com.selesse.android.winedb.R;
 import com.selesse.android.winedb.model.WineColor;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 public class Wine implements Serializable {
     private static final long serialVersionUID = 6500980482273835304L;
     public static final String TABLE_WINES = "wines";
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_BARCODE = "barcode";
     public static final String COLUMN_NAME = "name";
     public static final String COLUMN_RATING = "rating";
     public static final String COLUMN_COMMENT = "comment";
     public static final String COLUMN_COUNTRY = "country";
     public static final String COLUMN_DESCRIPTION = "description";
     public static final String COLUMN_IMAGE_URL = "imageUrl";
     public static final String COLUMN_PRICE = "price";
     public static final String COLUMN_YEAR = "year";
     public static final String COLUMN_COLOR = "color";
 
     public static final String[] FIELDS = {
             COLUMN_ID,
             COLUMN_BARCODE,
             COLUMN_NAME,
             COLUMN_RATING,
             COLUMN_COMMENT,
             COLUMN_COUNTRY,
             COLUMN_DESCRIPTION,
             COLUMN_IMAGE_URL,
             COLUMN_PRICE,
             COLUMN_YEAR,
             COLUMN_COLOR};
 
     private static final String DATABASE_CREATE = "create table " + TABLE_WINES + "(" + COLUMN_ID
             + " integer primary key autoincrement, " + COLUMN_BARCODE + " text, " + COLUMN_NAME
             + " text, " + COLUMN_RATING + " integer, " + COLUMN_COMMENT + " text, " + COLUMN_COUNTRY
             + " text, " + COLUMN_DESCRIPTION + " text, " + COLUMN_IMAGE_URL + " text," + COLUMN_PRICE
             + " text," + COLUMN_YEAR + " integer, " + COLUMN_COLOR + " text);";
 
     private long id = -1;
     private String barcode = "";
     private String name = "";
     private int rating = -1;
     private String comment = "";
     private String country = "";
     private String description = "";
     private String imageURL = "";
     private String price = "";
     private int year = -1;
     private WineColor color = WineColor.UNKNOWN;
 
     /*
      * No need to do anything, fields already have their default values.
      */
     public Wine() {}
 
     public Wine(final Cursor cursor) {
         this.id = cursor.getLong(0);
         this.barcode = cursor.getString(1);
         this.name = cursor.getString(2);
         this.rating = cursor.getInt(3);
         this.comment = cursor.getString(4);
         this.country = cursor.getString(5);
         this.description = cursor.getString(6);
         this.imageURL = cursor.getString(7);
         this.price = cursor.getString(8);
         this.year = cursor.getInt(9);
 
         try {
             this.color = WineColor.valueOf(cursor.getString(10).toUpperCase(Locale.getDefault()));
         } catch (IllegalArgumentException e) {
             this.color = WineColor.UNKNOWN;
         }
     }
 
     /**
      * Constructor used to "clone" a Wine. Will create a new {@link Wine} with the same field values as
      * the wine parameter passed to the constructor.
      *
      * @param wine The model Wine that we're essentially cloning.
      */
     public Wine(Wine wine) {
         this.id = wine.getId();
         this.barcode = wine.getBarcode();
         this.name = wine.getName();
         this.rating = wine.getRating();
         this.comment = wine.getComment();
         this.country = wine.getCountry();
         this.description = wine.getDescription();
         this.imageURL = wine.getImageUrl();
         this.price = wine.getPrice();
         this.year = wine.getYear();
         this.color = wine.getColor();
     }
 
     public ContentValues getContent() {
         final ContentValues values = new ContentValues();
         values.put(Wine.COLUMN_BARCODE, barcode);
         values.put(Wine.COLUMN_NAME, name);
         values.put(Wine.COLUMN_RATING, rating);
         values.put(Wine.COLUMN_COMMENT, comment);
         values.put(Wine.COLUMN_COUNTRY, country);
         values.put(Wine.COLUMN_DESCRIPTION, description);
         values.put(Wine.COLUMN_IMAGE_URL, imageURL);
         values.put(Wine.COLUMN_PRICE, price);
         values.put(Wine.COLUMN_YEAR, year);
         values.put(Wine.COLUMN_COLOR, color.toString());
         return values;
     }
 
     public static void onCreate(SQLiteDatabase database) {
         database.execSQL(DATABASE_CREATE);
     }
 
     public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
         // FIXME -> I doubt we want to kill the previous database
         database.execSQL("DROP TABLE IF EXISTS " + TABLE_WINES);
         onCreate(database);
     }
 
     public long getId() {
         return id;
     }
 
     public String getBarcode() {
         return barcode;
     }
 
     public String getName() {
         return name;
     }
 
     public int getRating() {
         return rating;
     }
 
     public String getComment() {
         return comment;
     }
 
     public String getCountry() {
         return country;
     }
 
     public String getDescription() {
         return description;
     }
 
     public String getImageUrl() {
         return imageURL;
     }
 
     public String getPrice() {
         return price;
     }
 
     public int getYear() {
         return year;
     }
 
     public WineColor getColor() {
         return color;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public void setBarcode(String barcode) {
         this.barcode = barcode;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setRating(int rating) {
         this.rating = rating;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public void setImageURL(String imageURL) {
         this.imageURL = imageURL;
     }
 
     public void setPrice(String price) {
         this.price = price;
     }
 
     public void setYear(int year) {
         this.year = year;
     }
 
     public void setColor(WineColor color) {
         this.color = color;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Wine wine = (Wine) o;
 
         if (id != wine.id) return false;
         if (rating != wine.rating) return false;
         if (year != wine.year) return false;
         if (barcode != null ? !barcode.equals(wine.barcode) : wine.barcode != null) return false;
         if (color != wine.color) return false;
         if (comment != null ? !comment.equals(wine.comment) : wine.comment != null) return false;
         if (country != null ? !country.equals(wine.country) : wine.country != null) return false;
         if (description != null ? !description.equals(wine.description) : wine.description != null)
             return false;
         if (imageURL != null ? !imageURL.equals(wine.imageURL) : wine.imageURL != null)
             return false;
         if (name != null ? !name.equals(wine.name) : wine.name != null) return false;
         if (price != null ? !price.equals(wine.price) : wine.price != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = (int) (id ^ (id >>> 32));
         result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
         result = 31 * result + (name != null ? name.hashCode() : 0);
         result = 31 * result + rating;
         result = 31 * result + (comment != null ? comment.hashCode() : 0);
         result = 31 * result + (country != null ? country.hashCode() : 0);
         result = 31 * result + (description != null ? description.hashCode() : 0);
         result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
         result = 31 * result + (price != null ? price.hashCode() : 0);
         result = 31 * result + year;
         result = 31 * result + (color != null ? color.hashCode() : 0);
         return result;
     }
 
     /**
      * Tells you whether or not the value of columnIndex is numeric in nature.
      *
      * @param columnIndex The DB column index of the wine.
      * @return Whether or not columnIndex is numeric.
      */
     public static boolean isNumericColumn(int columnIndex) {
         return columnIndex == 1 || columnIndex == 9;
     }
 
     public static boolean isColor(int columnIndex) {
         return columnIndex == 10;
     }
 
     public static List<String> getLocalizedSortStrings(Resources resources) {
         String name = resources.getString(R.string.wine_name);
         String rating = resources.getString(R.string.wine_rating);
         String year = resources.getString(R.string.wine_year);
         String country = resources.getString(R.string.wine_country);
         String color = resources.getString(R.string.wine_color);
         String price = resources.getString(R.string.wine_price);
         return Arrays.asList(name, rating, year, country, color, price);
     }
 
     @Override
     public String toString() {
         return "Wine{" +
                 "id=" + id +
                 ", barcode='" + barcode + '\'' +
                 ", name='" + name + '\'' +
                 ", rating=" + rating +
                 ", comment='" + comment + '\'' +
                 ", country='" + country + '\'' +
                 ", description='" + description + '\'' +
                 ", imageURL='" + imageURL + '\'' +
                 ", price='" + price + '\'' +
                 ", year=" + year +
                 ", color=" + color +
                 '}';
     }
 
     public int getNumberOfFilledFields() {
         int filledFields = 0;
 
        if (!barcode.equals("")) {
             filledFields++;
         }
        if (!name.equals("")) {
             filledFields++;
         }
         if (rating != -1) {
             filledFields++;
         }
        if (!comment.equals("")) {
             filledFields++;
         }
        if (!description.equals("")) {
            filledFields++;
         }
        if (!imageURL.equals("")) {
             filledFields++;
         }
        if (!price.equals("")) {
             filledFields++;
         }
         if (year != -1) {
             filledFields++;
         }
         if (color != WineColor.UNKNOWN) {
             filledFields++;
         }
 
         return filledFields;
     }
 }
