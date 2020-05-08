 package com.horeca;
 
 import android.provider.BaseColumns;
 
 public class HorecaContract {
 	// Prevents the FeedReaderContract class from being instantiated.
 	private HorecaContract() {}
 	/*
 	 * The `*_INDEX` are there for optimization.
 	 * If a query is done with COLOMN_NAMES as a selection,
 	 * to avoid calling getColumnIndex call or hard-coding the index,
 	 * the index in COLUMN_NAMES is in `*_INDEX`
 	 */
 	public static abstract class Ville implements BaseColumns {
 		public static final String TABLE_NAME = "villes";
 		public static final int _ID_INDEX = 0;
 		public static final String NAME = "nom";
 		public static final int NAME_INDEX = 1;
 		public static final String CODEPOSTAL = "codepostal";
 		public static final int CODEPOSTAL_INDEX = 2;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			NAME, CODEPOSTAL};
 	}
 	public static abstract class Horeca implements BaseColumns {
 		public static final String Q = "h";
 		public static final String TABLE_NAME = "horecas";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String _ID_Q = Q + "." + _ID;
 		public static final int _ID_INDEX = 0;
 		public static final String NAME = "nom";
 		public static final String NAME_Q = Q + "." + NAME;
 		public static final int NAME_INDEX = 1;
 		public static final String MIN_PRICE = "minprice";
 		public static final String MIN_PRICE_Q = Q + "." + MIN_PRICE;
 		public static final int MIN_PRICE_INDEX = 2;
 		public static final String MAX_PRICE = "maxprice";
 		public static final String MAX_PRICE_Q = Q + "." + MAX_PRICE;
 		public static final int MAX_PRICE_INDEX = 3;
 		public static final String LONGITUDE = "longitude";
 		public static final String LONGITUDE_Q = Q + "." + LONGITUDE;
 		public static final int LONGITUDE_INDEX = 4;
 		public static final String LATITUDE = "latitude";
 		public static final String LATITUDE_Q = Q + "." + LATITUDE;
 		public static final int LATITUDE_INDEX = 5;
 		public static final String VILLE_ID = "ville_id";
 		public static final String VILLE_ID_Q = Q + "." + VILLE_ID;
 		public static final int VILLE_ID_INDEX = 6;
 		public static final String NUMTEL = "numtel";
 		public static final String NUMTEL_Q = Q + "." + NUMTEL;
 		public static final int NUMTEL_INDEX = 7;
 		public static final String DESCRIPTION = "description";
 		public static final String DESCRIPTION_Q = Q + "." + DESCRIPTION;
 		public static final int DESCRIPTION_INDEX = 8;
 		public static final String IS_FAVORITE = "is_favorite";
 		public static final String IS_FAVORITE_Q = Q+ "." + IS_FAVORITE;
		public static final int IS_FAVORITE_INDEX = 9;
 		public static final String[] COLUMN_NAMES = new String[]{_ID, NAME, MIN_PRICE, MAX_PRICE, LONGITUDE, LATITUDE, VILLE_ID, NUMTEL, DESCRIPTION, IS_FAVORITE};
 		public static final String[] COLUMN_NAMES_Q = new String[]{_ID_Q, NAME_Q, MIN_PRICE_Q, MAX_PRICE_Q, LONGITUDE_Q, LATITUDE_Q, VILLE_ID_Q, NUMTEL_Q, DESCRIPTION_Q, IS_FAVORITE_Q};
 	}
 	
 	public static abstract class HorecaType implements BaseColumns {
 		public static final String Q = "ht";
 		public static final String TABLE_NAME = "horecatypes";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String _ID_Q = Q + "." + _ID;
 		public static final int _ID_INDEX = 0;
 		public static final String NAME = "nom";
 		public static final String NAME_Q = Q + "." + NAME;
 		public static final int NAME_INDEX = 1;
 		public static final String[] COLUMN_NAMES = new String[]{_ID, NAME};
 		public static final String[] COLUMN_NAMES_Q = new String[]{_ID_Q, NAME_Q};
 	}
 	public static abstract class HorecaTypeJoin implements BaseColumns {
 		public static final String Q = "htj";
 		public static final String TABLE_NAME = "horecatypejoins";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String HORECA_ID = "horeca_id";
 		public static final String HORECA_ID_Q = Q + "." + HORECA_ID;
 		public static final String HORECATYPE_ID = "horecatype_id";
 		public static final String HORECATYPE_ID_Q = Q + "." + HORECATYPE_ID;
 	}
 	public static abstract class Plat implements BaseColumns {
 		public static final String Q = "p";
 		public static final String TABLE_NAME = "plats";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String _ID_Q = Q + "." + _ID;
 		public static final int _ID_INDEX = 0;
 		public static final String HORECA_ID = "horeca_id";
 		public static final String HORECA_ID_Q = Q + "." + HORECA_ID;
 		public static final int HORECA_ID_INDEX = 1;
 		public static final String NAME = "nom";
 		public static final String NAME_Q = Q + "." + NAME;
 		public static final int NAME_INDEX = 2;
 		public static final String PRICE = "prix";
 		public static final String PRICE_Q = Q + "." + PRICE;
 		public static final int PRICE_INDEX = 3;
 		public static final String DESCRIPTION = "description";
 		public static final String DESCRIPTION_Q = Q + "." + DESCRIPTION;
 		public static final int DESCRIPTION_INDEX = 4;
 		public static final String STOCK = "stock";
 		public static final String STOCK_Q = Q + "." + STOCK;
 		public static final int STOCK_INDEX = 5;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			HORECA_ID, NAME, PRICE,
 			DESCRIPTION, STOCK};
 	}
 	public static abstract class PlatType implements BaseColumns {
 		public static final String Q = "pt";
 		public static final String TABLE_NAME = "plattypes";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String _ID_Q = Q + "." + _ID;
 		public static final int _ID_INDEX = 0;
 		public static final String NAME = "nom";
 		public static final String NAME_Q = Q + "." + NAME;
 		public static final int NAME_INDEX = 1;
 		public static final String[] COLUMN_NAMES = new String[]{_ID, NAME};
 		public static final String[] COLUMN_NAMES_Q = new String[]{_ID_Q, NAME_Q};
 	}
 	public static abstract class PlatTypeJoin implements BaseColumns {
 		public static final String Q = "ptj";
 		public static final String TABLE_NAME = "plattypejoins";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String PLAT_ID = "plat_id";
 		public static final String PLAT_ID_Q = Q + "." + PLAT_ID;
 		public static final String PLATTYPE_ID = "plattype_id";
 		public static final String PLATTYPE_ID_Q = Q + "." + PLATTYPE_ID;
 	}
 	public static abstract class Ingredient implements BaseColumns {
 		public static final String Q = "i";
 		public static final String TABLE_NAME = "ingredients";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String _ID_Q = Q + "." + _ID;
 		public static final int _ID_INDEX = 0;
 		public static final String NAME = "nom";
 		public static final String NAME_Q = Q + "." + NAME;
 		public static final int NAME_INDEX = 1;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			NAME};
 	}
 	public static abstract class User implements BaseColumns {
 		public static final String TABLE_NAME = "users";
 		public static final int _ID_INDEX = 0;
 		public static final String EMAIL = "email";
 		public static final int EMAIL_INDEX = 1;
 		public static final String NAME = "nom";
 		public static final int NAME_INDEX = 2;
 		public static final String PASSWORD = "password";
 		public static final int PASSWORD_INDEX = 3;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			EMAIL, NAME, PASSWORD};
 	}
 	public static abstract class Contient implements BaseColumns {
 		public static final String Q = "c";
 		public static final String TABLE_NAME = "contients";
 		public static final String TABLE_NAME_Q = TABLE_NAME + " " + Q;
 		public static final String _ID_Q = Q + "." + _ID;
 		public static final int _ID_INDEX = 0;
 		public static final String PLAT_ID = "plat_id";
 		public static final String PLAT_ID_Q = Q + "." + PLAT_ID;
 		public static final int PLAT_ID_INDEX = 1;
 		public static final String INGREDIENT_ID = "ingredient_id";
 		public static final String INGREDIENT_ID_Q = Q + "." + INGREDIENT_ID;
 		public static final int INGREDIENT_ID_INDEX = 2;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			PLAT_ID, INGREDIENT_ID};
 	}
 	public static abstract class Ouverture implements BaseColumns {
 		public static final String TABLE_NAME = "ouvertures";
 		public static final int _ID_INDEX = 0;
 		public static final String HORECA_ID = "horeca_id";
 		public static final int HORECA_ID_INDEX = 1;
 		public static final String DEBUT = "debut";
 		public static final int DEBUT_INDEX = 2;
 		public static final String FIN = "fin";
 		public static final int FIN_INDEX = 3;
 		public static final String PLACES = "places";
 		public static final int PLACES_INDEX = 4;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			HORECA_ID, DEBUT, FIN, PLACES};
 	}
 	public static abstract class Reservation implements BaseColumns {
 		public static final String TABLE_NAME = "reservations";
 		public static final int _ID_INDEX = 0;
 		public static final String USER_ID = "user_id";
 		public static final int USER_ID_INDEX = 1;
 		public static final String OUVERTURE_ID = "ouverture_id";
 		public static final int OUVERTURE_ID_INDEX = 2;
 		public static final String PLACES = "places";
 		public static final int PLACES_INDEX = 3;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			USER_ID, OUVERTURE_ID, PLACES};
 	}
 	public static abstract class Commande implements BaseColumns {
 		public static final String TABLE_NAME = "commandes";
 		public static final int _ID_INDEX = 0;
 		public static final String USER_ID = "user_id";
 		public static final int USER_ID_INDEX = 1;
 		public static final String PLAT_ID = "plat_id";
 		public static final int PLAT_ID_INDEX = 2;
 		public static final String TEMPS = "temps";
 		public static final int TEMPS_INDEX = 3;
 		public static final String NOMBRE = "nombres";
 		public static final int NOMBRE_INDEX = 4;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			USER_ID, PLAT_ID, TEMPS, NOMBRE};
 	}
 	public static abstract class Picture implements BaseColumns {
 		public static final String TABLE_NAME = "horecapictures"; 
 		public static final int _ID_INDEX = 0;
 		public static final String HORECA_ID = "horeca_id";
 		public static final int HOREC_ID_INDEX = 1;
 		public static final String PATH = "path_picture";
 		public static final int PATH_INDEX = 2;
 		public static final String NAME = "name_picture";
 		public static final int NAME_INDEX = 3;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			HORECA_ID, PATH, NAME};
 	}
 	public static abstract class Label implements BaseColumns {
 		public static final String TABLE_NAME = "ImageDescriptionLabel"; 
 		public static final int _ID_INDEX = 0;
 		public static final String PATH = "path_picture";
 		public static final int PATH_INDEX = 1;
 		public static final String NAME = "name_picture";
 		public static final int NAME_INDEX = 2;
 		public static final String DESCRIPTIONLABEL = "descriptionlabel";
 		public static final int DESCRIPTION_INDEX = 3;
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 		 PATH, NAME ,DESCRIPTIONLABEL};
 	}
 	public static abstract class LabelJoinHoreca implements BaseColumns {
 		public static final String TABLE_NAME = "LabelJoinHoreca"; 
 		public static final int _ID_INDEX = 0;
 		public static final String HORECA_ID = "horeca_id";
 		public static final int HORECA_ID_INDEX = 1;
 		public static final String LABEL_ID = "label_id";
 		public static final int LABEL_ID_INDEX = 2;
 		
 		public static final String[] COLUMN_NAMES = new String[]{_ID,
 			HORECA_ID, LABEL_ID};
 	}
 }
