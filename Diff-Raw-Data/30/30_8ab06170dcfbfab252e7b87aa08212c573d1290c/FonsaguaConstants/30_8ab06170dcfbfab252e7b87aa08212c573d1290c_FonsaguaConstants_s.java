 package es.udc.cartolab.gvsig.fonsagua.utils;
 
 public interface FonsaguaConstants {
 
     public static final String dataSchema = "fonsagua";
     public static final String baseSchema = "c_base";
 
     public static final String departamentosTable = "departamentos";
     public static final String departamentosPK = "cdpto";
     public static final String departamentosName = "dpto";
 
     public static final String municipiosTable = "municipios";
     public static final String municipiosPK = "cod_munic";
     public static final String municipiosName = "munic";
 
     public static final String cantonesTable = "cantones";
     public static final String cantonesPK = "cod_canton";
     public static final String cantonesName = "canton";
 
     public static final String GPS_MATCHING_FILES = "gps-matching-files";
 
     public static final String COMUNIDADES_IMPLICADAS = "comunidades_implicadas";
     public static final String[] COMUNIDADES_IMPLICADAS_COL_ALIAS = { "Nombre",
 	    "Habitantes totales", "Habitantes alternativa" };
     public static final String[] COMUNIDADES_IMPLICADAS_COL_NAMES = {
 	    "comunidad", "n_habitantes", "n_hab_alternativa" };
     public static final String FUENTES_IMPLICADAS = "fuentes_implicadas";
 
     public static final String AlternativesMap = "Vista alternativas";
     public static final String GeneralMap = "Vista general";
 
     public static final String TUBERIAS_COMERCIALES_NAME = "preferencias_tuberias";
     public static final String[] TUBERIAS_COMERCIALES_TABLE_FIELDS = {
 	    "id_tub", "material", "diametro", "presion", "rugosidad",
 	    "precio_m" };
     public static final String[] TUBERIAS_COMERCIALES_TABLE_ALIAS = { "ID",
 	    "Material", "Dimetro (mm)", "Presin (m.c.a)", "Rugosidad (D-W)",
 	    "Precio ($/m)" };
     public static final String BOMBAS_COMERCIALES_NAME = "preferencias_bombas";
     public static final String[] BOMBAS_COMERCIALES_TABLE_FIELDS = {
	    "id_bomba", "potencia", "precio_lmp" };
     public static final String[] BOMBAS_COMERCIALES_TABLE_ALIAS = { "ID",
 	    "Potencia (CV)", "Precio ($)" };
 
 }
