 package es.udc.cartolab.gvsig.fonsagua.utils;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 
 public class AlternativesPreferences {
 
     public static final String ALTERNATIVAS_PREFERENCES_TABLE = "preferencias_disenho";
     public static final String ALTERNATIVAS_PREFERENCES_PKFIELD = "cod_alternativa";
     public static final String[] ALTERNATIVAS_PREFERENCES_FIELDS = {
 	    "tasa_crecimiento", "ano_horiz_sistema", "ano_horiz_bomba",
 	    "dot_domiciliar", "dot_cantareras", "f_var_estacional",
 	    "f_var_horaria", "coef_q_ecologico", "n_integrantes_familia",
 	    "rendimiento_bomba", "pvp_kwh", "perdidas_puntual", "v_min",
 	    "v_max", "presion_min", "presion_max" };
     public static final String BOMBAS_TABLE = "preferencias_bombas";
     public static final String[] BOMBAS_FIELDS = { "id_bomba", "bomba",
	    "potencia", "precio_lmp" };
     public static final String TUBERIAS_TABLE = "preferencias_tuberias";
     public static final String[] TUBERIAS_FIELDS = { "id_tub", "denominacion",
 	    "material", "diametro", "presion", "rugosidad", "precio_lmp" };
 
     private static AlternativesPreferences instance = new AlternativesPreferences();
     private static List<Bomba> bombas;
     private static List<Tuberia> tuberias;
 
     private String codAlternativa = "";
     private double tasaCrecimiento = 2;
     private int anhoHorizSistema = 20;
     private int anhoHorizBomba = 10;
     private int dotDomiciliar = 90;
     private int dotCantareras = 40;
     private double fVarEstacional = 1.2;
     private double fVarHoraria = 2.25;
     private double coefQEcologico = 0.4;
     private double nIntegrantesFamilia = 6;
     private double rendimientoBomba = 0.6;
     private double pvpKwh = 4;
     private double perdidasPuntuales = 1;
     private double vMin = 0.5;
     private double vMax = 2;
     private int presionMin = 10;
     private int presionMax = 50;
 
     public static AlternativesPreferences getInstance() {
 	return instance;
     }
 
     public static void loadPreferences(String altCode) {
 	instance = new AlternativesPreferences(altCode);
     }
 
     private AlternativesPreferences() {
     }
 
     private AlternativesPreferences(String altCode) {
 	try {
 	    String[][] rows = DBSession.getCurrentSession().getTable(
 		    ALTERNATIVAS_PREFERENCES_TABLE,
 		    FonsaguaConstants.dataSchema,
 		    ALTERNATIVAS_PREFERENCES_FIELDS,
 		    "WHERE " + ALTERNATIVAS_PREFERENCES_PKFIELD + " = '"
 			    + codAlternativa + "'", new String[0], false);
 	    if (rows.length > 0) {
 		try {
 		    tasaCrecimiento = Double.parseDouble(rows[0][0]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    anhoHorizSistema = Integer.parseInt(rows[0][1]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    anhoHorizBomba = Integer.parseInt(rows[0][2]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    dotDomiciliar = Integer.parseInt(rows[0][3]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    dotCantareras = Integer.parseInt(rows[0][4]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    fVarEstacional = Double.parseDouble(rows[0][5]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    fVarHoraria = Double.parseDouble(rows[0][6]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    coefQEcologico = Double.parseDouble(rows[0][7]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    nIntegrantesFamilia = Double.parseDouble(rows[0][8]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    rendimientoBomba = Double.parseDouble(rows[0][9]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    pvpKwh = Double.parseDouble(rows[0][10]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    perdidasPuntuales = Double.parseDouble(rows[0][11]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    vMin = Double.parseDouble(rows[0][12]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    vMax = Double.parseDouble(rows[0][13]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    presionMin = Integer.parseInt(rows[0][14]);
 		} catch (NumberFormatException e) {
 		}
 		try {
 		    presionMax = Integer.parseInt(rows[0][15]);
 		} catch (NumberFormatException e) {
 		}
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
     }
 
     public static List<Bomba> getBombas() {
 	try {
 	    if (bombas == null) {
 		bombas = new ArrayList<Bomba>();
 		String[][] rows = DBSession.getCurrentSession().getTable(
 			BOMBAS_TABLE, FonsaguaConstants.dataSchema,
 			BOMBAS_FIELDS, "", BOMBAS_FIELDS, false);
 		for (String[] row : rows) {
 		    bombas.add(new Bomba(row[0], row[1], row[2], row[3]));
 		}
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return bombas;
     }
 
     public static List<Tuberia> getTuberias() {
 	try {
 	    if (tuberias == null) {
 		tuberias = new ArrayList<Tuberia>();
 		String[][] rows = DBSession.getCurrentSession().getTable(
 			TUBERIAS_TABLE, FonsaguaConstants.dataSchema,
 			TUBERIAS_FIELDS, "", TUBERIAS_FIELDS, false);
 		for (String[] row : rows) {
 		    tuberias.add(new Tuberia(row[0], row[1], row[2], row[3],
 			    row[4], row[5], row[6]));
 		}
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return tuberias;
     }
 
     public String getCodAlternativa() {
 	return codAlternativa;
     }
 
     public double getTasaCrecimiento() {
 	return tasaCrecimiento;
     }
 
     public int getAnhoHorizSistema() {
 	return anhoHorizSistema;
     }
 
     public int getAnhoHorizBomba() {
 	return anhoHorizBomba;
     }
 
     public int getDotDomiciliar() {
 	return dotDomiciliar;
     }
 
     public int getDotCantareras() {
 	return dotCantareras;
     }
 
     public double getfVarEstacional() {
 	return fVarEstacional;
     }
 
     public double getfVarHoraria() {
 	return fVarHoraria;
     }
 
     public double getCoefQEcologico() {
 	return coefQEcologico;
     }
 
     public double getnIntegrantesFamilia() {
 	return nIntegrantesFamilia;
     }
 
     public double getRendimientoBomba() {
 	return rendimientoBomba;
     }
 
     public double getPvpKwh() {
 	return pvpKwh;
     }
 
     public double getPerdidasPuntuales() {
 	return perdidasPuntuales;
     }
 
     public double getvMin() {
 	return vMin;
     }
 
     public double getvMax() {
 	return vMax;
     }
 
     public int getPresionMin() {
 	return presionMin;
     }
 
     public int getPresionMax() {
 	return presionMax;
     }
 
     public static class Bomba {
 	private String id;
 	private String denom;
 	private double potencia = 0;
 	private double precio = 0;
 
 	private Bomba(String id, String denom, String potencia, String precio) {
 	    this.id = id;
 	    this.denom = denom;
 	    try {
 		this.potencia = Double.parseDouble(potencia);
 	    } catch (NumberFormatException e) {
 	    }
 	    try {
 		this.precio = Double.parseDouble(precio);
 	    } catch (NumberFormatException e) {
 	    }
 	}
 
 	public String getId() {
 	    return id;
 	}
 
 	public String getDenom() {
 	    return denom;
 	}
 
 	public double getPotencia() {
 	    return potencia;
 	}
 
 	public double getPrecio() {
 	    return precio;
 	}
     }
 
     public static class Tuberia {
 	private String id;
 	private String denom;
 	private String material;
 	private double diametro = 0;
 	private double presion = 0;
 	private double rugosidad = 0;
 	private double precio = 0;
 
 	private Tuberia(String id, String denom, String material,
 		String diametro, String presion, String rugosidad, String precio) {
 	    this.id = id;
 	    this.denom = denom;
 	    this.material = material;
 	    try {
 		this.diametro = Double.parseDouble(diametro);
 	    } catch (NumberFormatException e) {
 	    }
 	    try {
 		this.presion = Double.parseDouble(presion);
 	    } catch (NumberFormatException e) {
 	    }
 	    try {
 		this.rugosidad = Double.parseDouble(rugosidad);
 	    } catch (NumberFormatException e) {
 	    }
 	    try {
 		this.precio = Double.parseDouble(precio);
 	    } catch (NumberFormatException e) {
 	    }
 	}
 
 	public String getId() {
 	    return id;
 	}
 
 	public String getDenom() {
 	    return denom;
 	}
 
 	public String getMaterial() {
 	    return material;
 	}
 
 	public double getDiametro() {
 	    return diametro;
 	}
 
 	public double getPresion() {
 	    return presion;
 	}
 
 	public double getRugosidad() {
 	    return rugosidad;
 	}
 
 	public double getPrecio() {
 	    return precio;
 	}
     }
 }
