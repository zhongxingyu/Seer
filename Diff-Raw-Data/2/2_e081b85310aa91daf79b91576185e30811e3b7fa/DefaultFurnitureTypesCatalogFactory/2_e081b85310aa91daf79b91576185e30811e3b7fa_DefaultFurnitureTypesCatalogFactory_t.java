 package interiores.business.models.catalogs.factories;
 
 import interiores.business.models.room.FurnitureModel;
 import interiores.business.models.room.FurnitureType;
 import interiores.business.models.SpaceAround;
 import interiores.business.models.catalogs.NamedCatalog;
 import interiores.core.business.BusinessException;
 import interiores.utils.Dimension;
 import interiores.utils.Range;
 
 /**
  *
  * @author hector
  */
 public class DefaultFurnitureTypesCatalogFactory
 {
     public static NamedCatalog<FurnitureType> getCatalog() throws BusinessException {
         NamedCatalog<FurnitureType> catalog = new NamedCatalog();
         
         catalog.add(bedDouble());
         catalog.add(bedSingle());
         catalog.add(tableBedside());
         catalog.add(bureau());
         catalog.add(cabinet());
         catalog.add(mirror());
         catalog.add(sink());
         catalog.add(oven());
         catalog.add(microwave());
         catalog.add(hob());
         catalog.add(stove());
         catalog.add(extractor());
         catalog.add(fridge());
         catalog.add(dishwasher());
         catalog.add(washer());
         catalog.add(shelf());
         catalog.add(trash());
         catalog.add(bidet());
         catalog.add(washbasin());
         catalog.add(toilet());
         catalog.add(bathtub());
         catalog.add(shower());
         catalog.add(dryer());
         catalog.add(table());
         catalog.add(chair());
         catalog.add(armchair());
         catalog.add(worktop());
         catalog.add(wringer());
         catalog.add(sofa());
         catalog.add(shelves());
         catalog.add(rack());
         catalog.add(tv());
         catalog.add(buffet());
         
         return catalog;
     }
     
     private static FurnitureType bedDouble() throws BusinessException {
         FurnitureType ft = new FurnitureType("bedDouble", new Range(120, 240), new Range(180, 210),
                 new SpaceAround(50, 50, 0, 50));
         
         ft.addFurnitureModel(new FurnitureModel("SFORANVIK", new Dimension(140, 205), 269f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("ARONYN", new Dimension(140, 205), 229f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SLL", new Dimension(140, 205), 359f, "glazed",
                 "birch"));
         ft.addFurnitureModel(new FurnitureModel("KAMMELLI", new Dimension(150, 190), 349f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("SLAL", new Dimension(160, 205), 239f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("PEKKKA", new Dimension(200, 205), 239f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("NYALLL", new Dimension(140, 200), 179f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SFLATA", new Dimension(160, 205), 149f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("KSAYERII", new Dimension(150, 190), 199f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("IRMMGARD", new Dimension(140, 190), 189f, "black",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType bedSingle() throws BusinessException {
         FurnitureType ft = new FurnitureType("bedSingle", new Range(70, 150), new Range(180, 210),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("TERESJI", new Dimension(90, 190), 129.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("BJOJANA", new Dimension(90, 205), 169.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("DAMMH", new Dimension(80, 190), 169.99f, "glazed",
                "pine"));
         ft.addFurnitureModel(new FurnitureModel("BJERNNT", new Dimension(80, 205), 169.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("RR", new Dimension(80, 205), 189.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("EGBJERT", new Dimension(90, 200), 189.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("FJERGAL", new Dimension(80, 200), 99.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("RADDKKA", new Dimension(90, 190), 89.99f, "brown",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType tableBedside() throws BusinessException {
         FurnitureType ft = new FurnitureType("tableBedside", new Range(20, 80), new Range(20, 70));
         
         ft.addFurnitureModel(new FurnitureModel("YANVIK", new Dimension(60, 40), 23.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("ENOK", new Dimension(40, 40), 36.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("BJOIKKO", new Dimension(40, 40), 30.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MATYLDD", new Dimension(60, 55), 79.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("TAFARI", new Dimension(60, 30), 38.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SFKWYPP", new Dimension(60, 55), 69.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("GEERTJ", new Dimension(60, 55), 79.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SNESVSJA", new Dimension(60, 40), 69.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("TAILLANVIK", new Dimension(45, 30), 49.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("GASVRAYL", new Dimension(55, 40), 32.99f, "glazed",
                 "pine"));
         
         return ft;
     }
 
     private static FurnitureType bureau() throws BusinessException {
         FurnitureType ft = new FurnitureType("bureau", new Range(40, 200), new Range(20, 100));
         
         ft.addFurnitureModel(new FurnitureModel("BJYLJANNA", new Dimension(55, 45), 48.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("HIPOLIT", new Dimension(60, 45), 79.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("DAITORD", new Dimension(50, 50), 69.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("TJEMMA", new Dimension(60, 45), 129.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("ANNGELA", new Dimension(50, 40), 59.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MYRJANA", new Dimension(70, 40), 89.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SFYNNIELU", new Dimension(105, 45), 139.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("AN", new Dimension(60, 45), 69.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SFYSVKA", new Dimension(60, 45), 79.99f, "glazed",
                 "birch"));
         ft.addFurnitureModel(new FurnitureModel("NIKKO", new Dimension(110, 50), 89.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ELLIZABJETA", new Dimension(80, 45), 69.99f, "blue",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType cabinet() throws BusinessException {
         FurnitureType ft = new FurnitureType("cabinet", new Range(40, 250), new Range(25, 90));
         
         ft.addFurnitureModel(new FurnitureModel("JASMINN", new Dimension(190, 50), 189.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SAIYUERD", new Dimension(110, 45), 89.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("HENRYK", new Dimension(80, 50), 99.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("PALLI", new Dimension(160, 40), 169.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("JOAKYMKE", new Dimension(160, 50), 229.4f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("TAS", new Dimension(85, 40), 59.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("EDDANVIK", new Dimension(185, 60), 149.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SLOFFYSVA", new Dimension(160, 45), 139.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("INNGALF", new Dimension(115, 40), 79.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ERLANTZ", new Dimension(100, 35), 99.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("WILHELLMINN", new Dimension(60, 60), 69.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("BRYT", new Dimension(100, 60), 149.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("JARASJLLAFA", new Dimension(195, 50), 239.8f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("AFRYSSA", new Dimension(140, 60), 219.6f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("MELISJSJA", new Dimension(195, 60), 219.8f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ALLIFERD", new Dimension(95, 40), 219f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("YRSVULLA", new Dimension(65, 50), 89.99f, "gray",
                 "agglomerate"));    
         ft.addFurnitureModel(new FurnitureModel("BJANYFAAS", new Dimension(90, 30), 69.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("ADDEBAYALL", new Dimension(65, 40), 21.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MEENN", new Dimension(90, 30), 37.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ARFID", new Dimension(60, 60), 89.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SLYSIANVIK", new Dimension(60, 60), 59.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("NEFENA", new Dimension(110, 45), 99.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("RYMENNA", new Dimension(90, 50), 89.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("RALANVIK", new Dimension(90, 40), 36.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SNTAMMBJI", new Dimension(80, 50), 49.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SVESJNA", new Dimension(50, 50), 79.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("AA", new Dimension(60, 60), 59.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SLARKE", new Dimension(90, 35), 89.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SKAANVIK", new Dimension(110, 55), 89.99f, "glazed",
                 "pine"));
         
         return ft;
     }
     
     private static FurnitureType mirror() throws BusinessException {
         FurnitureType ft = new FurnitureType("mirror", new Range(30, 200), new Range(5, 5),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("NONN", new Dimension(80, 5), 49.99f, "gray",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("TOIRDHELBAK", new Dimension(30, 5), 6.99f, "white",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("SAFI", new Dimension(35, 5), 49.99f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("PI", new Dimension(100, 5), 109.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("MART", new Dimension(90, 5), 59.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("BETRYX", new Dimension(50, 5), 69.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("TANELLI", new Dimension(60, 5), 59.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("RAGNFALD", new Dimension(90, 5), 31.99f, "white",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("STJEIND", new Dimension(80, 5), 89.99f, "blue",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType sink() throws BusinessException {
         FurnitureType ft = new FurnitureType("sink", new Range(55, 150), new Range(48, 64),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("TAYKA", new Dimension(110, 50), 179.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SFAHRI", new Dimension(80, 55), 119.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("TJEADDARD", new Dimension(115, 50), 89.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("KOMGAL", new Dimension(100, 60), 189.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("HERMANDORD", new Dimension(120, 60), 219f, "gray",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType oven() throws BusinessException {
         FurnitureType ft = new FurnitureType("oven", new Range(50, 70), new Range(40, 60),
                 new SpaceAround(30, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("NOHN", new Dimension(60, 55), 259f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SVILFFIO", new Dimension(60, 55), 239f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SVUREK", new Dimension(60, 55), 209f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("TUYLLA", new Dimension(60, 55), 229f, "gray",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType microwave() throws BusinessException {
         FurnitureType ft = new FurnitureType("microwave", new Range(50, 70), new Range(35, 60),
                 new SpaceAround(30, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("SEIJA", new Dimension(60, 35), 229f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("ALEKSVANTORD", new Dimension(60, 35), 99.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("MAXIMMILLI", new Dimension(60, 35), 99.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("FJYANNDORD", new Dimension(60, 35), 99.99f, "gray",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType hob() throws BusinessException {
         FurnitureType ft = new FurnitureType("hob", new Range(50, 70), new Range(40, 60),
                 new SpaceAround(60, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("HOA", new Dimension(60, 50), 279f, "black",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("ANNTONYN", new Dimension(60, 50), 309f, "black",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("NYRU", new Dimension(60, 50), 199.99f, "black",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("SAMMYEL", new Dimension(60, 50), 419f, "gray",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("GORDDANVIK", new Dimension(60, 50), 419f, "black",
                 "glass"));
         
         return ft;
     }
 
     private static FurnitureType stove() throws BusinessException {
         FurnitureType ft = new FurnitureType("stove", new Range(50, 75), new Range(40, 60),
                 new SpaceAround(60, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("KRSTO", new Dimension(60, 50), 199.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("ALBINN", new Dimension(75, 50), 219f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SLATARVIK", new Dimension(60, 50), 149.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("GLORYNTA", new Dimension(70, 50), 339f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SLYUK", new Dimension(75, 50), 309f, "gray",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType extractor() throws BusinessException {
         FurnitureType ft = new FurnitureType("extractor", new Range(50, 90), new Range(40, 60));
         
         ft.addFurnitureModel(new FurnitureModel("MYRKE", new Dimension(60, 50), 249f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("MODDESJT", new Dimension(60, 50), 389f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("MARI", new Dimension(90, 50), 359f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("MALLAKKII", new Dimension(60, 45), 229f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("JELYSVAFETA", new Dimension(90, 50), 209f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SKANRAD", new Dimension(90, 50), 279f, "gray",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType fridge() throws BusinessException {
         FurnitureType ft = new FurnitureType("fridge", new Range(50, 70), new Range(40, 60));
         
         ft.addFurnitureModel(new FurnitureModel("ARZU", new Dimension(60, 60), 389f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("ADDELLA", new Dimension(60, 60), 269f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("HILLTRAD", new Dimension(60, 60), 369f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SNONNTLL", new Dimension(60, 60), 279f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("KYASI", new Dimension(60, 60), 479f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("MYADDRAG", new Dimension(60, 60), 549f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("DEYRDDR", new Dimension(60, 60), 499f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SVLLADDIMKE", new Dimension(60, 60), 459f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("ELLYF", new Dimension(60, 60), 419f, "white",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType dishwasher() throws BusinessException {
         FurnitureType ft = new FurnitureType("dishwasher", new Range(50, 70), new Range(40, 60));
         
         ft.addFurnitureModel(new FurnitureModel("PASJKAL", new Dimension(60, 55), 249f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("RAK", new Dimension(60, 55), 229f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("TEREZA", new Dimension(60, 60), 249f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("EDDMYND", new Dimension(60, 55), 259f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SERPYL", new Dimension(60, 55), 219f, "white",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType washer() throws BusinessException {
         FurnitureType ft = new FurnitureType("washer", new Range(50, 70), new Range(40, 60),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("SLIUBJAMIR", new Dimension(60, 55), 219f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("REIND", new Dimension(60, 60), 219f, "black",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("HENRIKA", new Dimension(60, 55), 279f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SFRYSTII", new Dimension(60, 60), 229f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("WYLHELLMKE", new Dimension(60, 55), 389f, "white",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType shelf() throws BusinessException {
         FurnitureType ft = new FurnitureType("shelf", new Range(30, 180), new Range(5, 50),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("KTYRAD", new Dimension(70, 10), 37.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("BJERTRAMKE", new Dimension(70, 16), 20.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("REBJEKKSSA", new Dimension(100, 10), 28.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("ESVTERKE", new Dimension(75, 12), 39.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("ANA", new Dimension(65, 10), 19.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("DRAGO", new Dimension(95, 12), 24.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("RENNARD", new Dimension(90, 17), 28.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("EMMA", new Dimension(65, 13), 27.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("RETO", new Dimension(110, 13), 30.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SEBASTIANVIK", new Dimension(70, 10), 17.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("DIDA", new Dimension(75, 15), 24.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SFAHARINN", new Dimension(75, 18), 20.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ESVMKE", new Dimension(105, 14), 59.99f, "glazed",
                 "birch"));
         ft.addFurnitureModel(new FurnitureModel("PAITRANNELLA", new Dimension(115, 11), 31.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("DOMMINYKKA", new Dimension(50, 22), 34.99f, "glazed",
                 "birch"));
         
         return ft;
     }
 
     private static FurnitureType trash() throws BusinessException {
         FurnitureType ft = new FurnitureType("trash", new Range(20, 50), new Range(20, 50),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("DAIDDERIKKH", new Dimension(40, 40), 8.99f, "red",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SFYDDAIGUU", new Dimension(40, 40), 13.99f, "orange",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("TANELLI", new Dimension(35, 35), 12.99f, "orange",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("ELYZA", new Dimension(30, 30), 15.99f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("DYRKEN", new Dimension(30, 30), 7.99f, "gray",
                 "plastic"));
         
         return ft;
     }
 
     private static FurnitureType bidet() throws BusinessException {
         FurnitureType ft = new FurnitureType("bidet", new Range(30, 50), new Range(40, 60),
                 new SpaceAround(50, 25, 0, 25));
         
         ft.addFurnitureModel(new FurnitureModel("ALLEKKSVEI", new Dimension(40, 60), 89.99f, "gray",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SVYLMMA", new Dimension(40, 60), 159.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("GREGARD", new Dimension(40, 60), 189.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("RAMONNA", new Dimension(40, 60), 149.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("DEKEBJAL", new Dimension(40, 60), 209f, "gray",
                 "porcelain"));
         
         return ft;
     }
 
     private static FurnitureType washbasin() throws BusinessException {
         FurnitureType ft = new FurnitureType("washbasin", new Range(40, 60), new Range(30, 60),
                 new SpaceAround(50, 20, 0, 20));
         
         ft.addFurnitureModel(new FurnitureModel("SKLLADDIYSJZ", new Dimension(55, 45), 37.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SFYNYEYKKEN", new Dimension(50, 40), 49.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("HELLENA", new Dimension(45, 35), 49.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("MIKEL", new Dimension(60, 55), 79.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SADDB", new Dimension(55, 50), 39.99f, "blue",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SFARLLOTT", new Dimension(60, 50), 39.99f, "gray",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("BRYGYD", new Dimension(55, 50), 49.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("ETNNA", new Dimension(60, 55), 59.99f, "gray",
                 "porcelain"));
         
         return ft;
     }
 
     private static FurnitureType toilet() throws BusinessException {
         FurnitureType ft = new FurnitureType("toilet", new Range(40, 60), new Range(50, 80),
                 new SpaceAround(50, 25, 0, 25));
         
         ft.addFurnitureModel(new FurnitureModel("SVOJTEKH", new Dimension(55, 70), 219f, "black",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SNYKKOLLAS", new Dimension(40, 75), 299f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("KARLLA", new Dimension(55, 75), 139.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("AYMMO", new Dimension(40, 75), 299f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SVID", new Dimension(55, 75), 179.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SLUUK", new Dimension(40, 75), 109.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("HEINNRYKH", new Dimension(55, 75), 159.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("BARBARKE", new Dimension(55, 75), 89.99f, "black",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("RODDIA", new Dimension(40, 75), 109.99f, "white",
                 "porcelain"));
         
         return ft;
     }
 
     private static FurnitureType bathtub() throws BusinessException {
         FurnitureType ft = new FurnitureType("bathtub", new Range(60, 90), new Range(100, 210));
         
         ft.addFurnitureModel(new FurnitureModel("SLARS", new Dimension(70, 160), 129f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("META", new Dimension(70, 190), 329f, "black",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("RYYD", new Dimension(70, 175), 259f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("DAGNNII", new Dimension(70, 170), 149f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("MIKKITA", new Dimension(75, 170), 319f, "gray",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("MYTYSSA", new Dimension(80, 170), 239f, "black",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SIGN", new Dimension(70, 175), 319f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("STJIANVIK", new Dimension(80, 170), 239f, "white",
                 "porcelain"));
         
         return ft;
     }
 
     private static FurnitureType shower() throws BusinessException {
         FurnitureType ft = new FurnitureType("shower", new Range(60, 120), new Range(60, 120),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("EMMILIIA", new Dimension(90, 120), 139.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("KIYRKE", new Dimension(70, 90), 119.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SLEA", new Dimension(70, 100), 109.99f, "gray",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("MARGARETA", new Dimension(70, 100), 99.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("SALAM", new Dimension(70, 70), 69.99f, "black",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("STJELLARKE", new Dimension(80, 100), 129.99f, "white",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("ADDELA", new Dimension(70, 70), 79.99f, "black",
                 "porcelain"));
         ft.addFurnitureModel(new FurnitureModel("FJINYANVIK", new Dimension(70, 100), 109.99f, "white",
                 "porcelain"));
         
         return ft;
     }
 
     private static FurnitureType dryer() throws BusinessException {
         FurnitureType ft = new FurnitureType("dryer", new Range(50, 70), new Range(40, 60),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("KELLAGH", new Dimension(60, 55), 239f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("WYKTARD", new Dimension(60, 55), 299f, "white",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SLUGHAYDDH", new Dimension(60, 60), 389f, "black",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SKATJA", new Dimension(60, 55), 269f, "white",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType table() throws BusinessException {
         FurnitureType ft = new FurnitureType("table", new Range(40, 300), new Range(30, 500),
                 new SpaceAround(60, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("JENGO", new Dimension(90, 140), 59.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("ATA", new Dimension(70, 110), 59.99f, "brown",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("EMIL", new Dimension(115, 190), 69.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("KORINN", new Dimension(145, 160), 149.99f, "glazed",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("HANNARATA", new Dimension(75, 130), 119.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SVYNNKKENNC", new Dimension(140, 160), 119.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("AILI", new Dimension(120, 230), 169.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("AAPPELI", new Dimension(105, 155), 79.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("WAIB", new Dimension(145, 195), 109.99f, "blue",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("AGATA", new Dimension(120, 185), 129.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("ROBJERT", new Dimension(80, 135), 169.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("FJLARYS", new Dimension(90, 160), 169.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("WALDEMARVIK", new Dimension(115, 160), 119.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("JERKORD", new Dimension(120, 180), 69.99f, "gray",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("SNIKOLLA", new Dimension(95, 185), 129.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("BJERGLLYAT", new Dimension(115, 130), 36.99f, "green",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("SVULLIJANA", new Dimension(135, 195), 69.99f, "white",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("KONARD", new Dimension(90, 155), 139.99f, "blue",
                 "glass"));
         ft.addFurnitureModel(new FurnitureModel("SKRISTYINN", new Dimension(100, 180), 99.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("PAFLLA", new Dimension(120, 135), 119.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("ANYKKA", new Dimension(70, 110), 39.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ANNTTONNI", new Dimension(55, 70), 49.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SVERONYKA", new Dimension(50, 40), 59.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SSANIU", new Dimension(55, 30), 79.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("BJO", new Dimension(50, 30), 49.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("TINNEK", new Dimension(40, 30), 59.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("GERTY", new Dimension(60, 30), 49.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("TIGO", new Dimension(40, 35), 26.99f, "black",
                 "plastic"));
         
         return ft;
     }
 
     private static FurnitureType chair() throws BusinessException {
         FurnitureType ft = new FurnitureType("chair", new Range(40, 60), new Range(40, 60),
                 new SpaceAround(30, 20, 0, 20));
         
         ft.addFurnitureModel(new FurnitureModel("SAMMHAYRLL", new Dimension(45, 45), 14.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("MYTJA", new Dimension(45, 45), 12.99f, "white",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("MARGARETA", new Dimension(50, 50), 27.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ILLSA", new Dimension(45, 45), 16.99f, "white",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("EMORD", new Dimension(45, 45), 10.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("NADDA", new Dimension(50, 50), 22.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("MOIRRI", new Dimension(45, 45), 20.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("HEDDFFIG", new Dimension(50, 50), 34.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SABIENRA", new Dimension(50, 50), 18.99f, "yellow",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("ALIMPI", new Dimension(50, 50), 27.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("AMALI", new Dimension(50, 50), 29.99f, "gray",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType armchair() throws BusinessException {
         FurnitureType ft = new FurnitureType("armchair", new Range(40, 80), new Range(40, 80),
                 new SpaceAround(40, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("JOSUNN", new Dimension(60, 60), 38.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SKATJA", new Dimension(70, 70), 59.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ARKKAITZ", new Dimension(70, 70), 59.99f, "blue",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType worktop() throws BusinessException {
         FurnitureType ft = new FurnitureType("worktop", new Range(5, 300), new Range(60, 80),
                 new SpaceAround(60, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("MIHAELLA", new Dimension(35, 60), 17.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("SVYRGYLYU", new Dimension(40, 60), 20.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("GYNNTRAMKE", new Dimension(45, 60), 22.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("SEPPPPO", new Dimension(50, 60), 25.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("SYGRUND", new Dimension(55, 60), 27.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("WIBJO", new Dimension(60, 60), 30.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("EDDFARD", new Dimension(80, 60), 49.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("HELMMA", new Dimension(90, 60), 49.99f, "white",
                 "marble"));
         ft.addFurnitureModel(new FurnitureModel("RYFINN", new Dimension(100, 60), 59.99f, "white",
                 "marble"));
         
         return ft;
     }
 
     private static FurnitureType wringer() throws BusinessException {
         FurnitureType ft = new FurnitureType("wringer", new Range(50, 70), new Range(50, 60),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("MARTIND", new Dimension(60, 60), 59.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("MAAYKKEN", new Dimension(60, 60), 59.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("EDDYT", new Dimension(60, 60), 49.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("GERTRAD", new Dimension(60, 60), 69.99f, "gray",
                 "steel"));
         
         return ft;
     }
 
     private static FurnitureType sofa() throws BusinessException {
         FurnitureType ft = new FurnitureType("sofa", new Range(60, 300), new Range(40, 80),
                 new SpaceAround(20, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("SYSVANNDORD", new Dimension(155, 60), 329.99f, "black",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SJEF", new Dimension(180, 60), 599.60f, "white",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("SLEONNOR", new Dimension(130, 60), 79.99f, "red",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("MARGAREETA", new Dimension(180, 60), 199.99f, "green",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("ANANVIK", new Dimension(155, 60), 359.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("KOMGANVIK", new Dimension(155, 60), 299.99f, "white",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SVEDDRANA", new Dimension(210, 60), 329.99f, "red",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("MYROSVLASVA", new Dimension(150, 60), 269.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("DALLIBORKKA", new Dimension(175, 60), 209.99f, "yellow",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("AISJU", new Dimension(180, 60), 189.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("KRYSJTINAN", new Dimension(220, 60), 309.99f, "red",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("TJYLO", new Dimension(205, 55), 229.10f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("RAZA", new Dimension(180, 60), 239.30f, "brown",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("SVONYN", new Dimension(210, 60), 189.99f, "gray",
                 "steel"));
         ft.addFurnitureModel(new FurnitureModel("DAFFARKKA", new Dimension(145, 60), 149.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MEYNTJ", new Dimension(145, 60), 369.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SLYDDIIA", new Dimension(210, 60), 469.40f, "red",
                 "beech"));
         ft.addFurnitureModel(new FurnitureModel("EMILI", new Dimension(125, 60), 179.99f, "white",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("JOZEF", new Dimension(130, 55), 229.20f, "white",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SFYOMA", new Dimension(210, 60), 199.99f, "blue",
                 "pine"));
         
         return ft;
     }
 
     private static FurnitureType shelves() throws BusinessException {
         FurnitureType ft = new FurnitureType("shelves", new Range(30, 400), new Range(20, 70),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("GARAYL", new Dimension(250, 40), 349f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SVYSVEKKA", new Dimension(210, 30), 159f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("IBRAHYMMA", new Dimension(125, 30), 329f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SVALERII", new Dimension(135, 30), 249f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("BERYT", new Dimension(105, 40), 359f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("AUGUSTYS", new Dimension(165, 35), 279f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ANNITA", new Dimension(110, 50), 349f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("IDDA", new Dimension(145, 24), 269f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("WOLF", new Dimension(40, 55), 199.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MARJANNI", new Dimension(235, 30), 339f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("POLINN", new Dimension(180, 30), 239f, "green",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("ANNELI", new Dimension(55, 30), 49.99f, "white",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("MARTINN", new Dimension(135, 25), 199.99f, "blue",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType rack() throws BusinessException {
         FurnitureType ft = new FurnitureType("rack", new Range(20, 80), new Range(20, 80),
                 new SpaceAround(30, 0, 30, 0));
         
         ft.addFurnitureModel(new FurnitureModel("SKARD", new Dimension(45, 45), 79.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("DANYSSA", new Dimension(60, 60), 59.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MYLANNA", new Dimension(40, 40), 89.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("SEKKNALLL", new Dimension(50, 50), 99.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("RYDDOLLF", new Dimension(60, 60), 79.99f, "brown",
                 "agglomerate"));
         
         return ft;
     }
 
     private static FurnitureType tv() throws BusinessException {
         FurnitureType ft = new FurnitureType("tv", new Range(40, 100), new Range(5, 70),
                 new SpaceAround(100, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("HELG", new Dimension(80, 30), 149.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("SLATARVIK", new Dimension(85, 10), 299f, "gray",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("GORDDANVIK", new Dimension(55, 45), 129.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("WITOLLD", new Dimension(50, 40), 119.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("SFREDDRYK", new Dimension(80, 5), 319f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("SNKEKKI", new Dimension(55, 35), 99.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("TEODDARD", new Dimension(85, 45), 129.99f, "black",
                 "plastic"));
         ft.addFurnitureModel(new FurnitureModel("ARISJA", new Dimension(75, 30), 99.99f, "gray",
                 "plastic"));
         
         return ft;
     }
 
     private static FurnitureType buffet() throws BusinessException {
         FurnitureType ft = new FurnitureType("buffet", new Range(20, 100), new Range(20, 100),
                 new SpaceAround(50, 0, 0, 0));
         
         ft.addFurnitureModel(new FurnitureModel("GARANVIK", new Dimension(35, 45), 59.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("ALLF", new Dimension(55, 65), 69.99f, "brown",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("TOMMA", new Dimension(40, 50), 89.99f, "glazed",
                 "birch"));
         ft.addFurnitureModel(new FurnitureModel("BARBARKE", new Dimension(65, 65), 99.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("BJORISJLLAFA", new Dimension(45, 55), 59.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SVAANNDORD", new Dimension(40, 40), 39.99f, "gray",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("DARINN", new Dimension(50, 50), 24.99f, "blue",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SLOFFYSVA", new Dimension(75, 75), 109.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SVOAKKYMKE", new Dimension(60, 65), 35.99f, "white",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SNEDDJELLJKO", new Dimension(70, 80), 69.99f, "glazed",
                 "pine"));
         ft.addFurnitureModel(new FurnitureModel("MIRKE", new Dimension(70, 80), 49.99f, "black",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("MALLAKKII", new Dimension(55, 65), 49.99f, "red",
                 "agglomerate"));
         ft.addFurnitureModel(new FurnitureModel("SAFFA", new Dimension(30, 35), 69.99f, "black",
                 "agglomerate"));
         
         return ft;
     }
 }
