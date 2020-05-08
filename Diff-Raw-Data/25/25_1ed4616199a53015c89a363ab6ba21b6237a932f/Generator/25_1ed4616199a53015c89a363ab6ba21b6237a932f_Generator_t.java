 package de.datagenerator.generator;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.datagenerator.DBModule;
 import de.datagenerator.creator.Creator;
 import de.datagenerator.datamodel.Product;
 import de.datagenerator.dbwriter.IDBWriter;
 import de.datagenerator.timekeeper.KundeTimeKeeper;
 import de.datagenerator.timekeeper.ProduktTimeKeeper;
 import de.datagenerator.timekeeper.TimeKeeper;
 import de.datagenerator.timekeeper.WKorbProduktTimeKeeper;
 import de.datagenerator.timekeeper.WKorbTimeKeepter;
 
 public class Generator {
 
    private Creator creator;
    private Product product;
    private IDBWriter dbWriter;
    private Generator strategy;
    private Injector injector = Guice.createInjector(new DBModule());
 
    public void generate(final long aKunden, final long aProdukte,
                         final long aWKoerbeProKunde,
                         final long aProdukteInWarenkorb) {
       
       System.out.println(aKunden + " Kunden | " + aProdukte + " Produkte | "
          + aWKoerbeProKunde + " Warenkoerbe pro Kunde | "
          + aProdukteInWarenkorb + " Produkte im Warenkorb");
       
       TimeKeeper.setParameter(aKunden, aProdukte, aWKoerbeProKunde,
                               aProdukteInWarenkorb);
       
       generateProducts(aKunden, aProdukte, aWKoerbeProKunde,
                        aProdukteInWarenkorb);
    }
 
    private void generateProducts(final long aKunden, final long aProdukte,
                                  final long aWKoerbeProKunde,
                                  final long aProdukteInWarenkorb) {
       generateKunden(aKunden);
       generateProdukte(aProdukte);
       generateWarenkorb(aKunden, aWKoerbeProKunde);
       generateProdukteInWKorb(aKunden * aWKoerbeProKunde * aProdukteInWarenkorb,
                               aProdukte, aProdukteInWarenkorb);
    }
 
   protected void generateKunden(final long aKunden) {
       KundeTimeKeeper.startTimeKunde();
       strategy = injector.getInstance(KundenGenerator.class);
       strategy.generateKunden(aKunden);
       KundeTimeKeeper.endTime();
    };
 
   protected void generateProdukte(final long aProdukte) {
       ProduktTimeKeeper.startTimeProdukt();
       strategy = injector.getInstance(ProduktGenerator.class);
       strategy.generateProdukte(aProdukte);
       ProduktTimeKeeper.endTime();
    };
 
   protected void generateWarenkorb(final long aKunden,
                                  final long aWarenkoerbeProKunde) {
       WKorbTimeKeepter.startTimeWKorb();
       strategy = injector.getInstance(WKorbGenerator.class);
       strategy.generateWarenkorb(aKunden, aWarenkoerbeProKunde);
       WKorbTimeKeepter.endTime();
    };
 
   protected void generateProdukteInWKorb(final long aBestellzeilen,
                                        final long aProdukte,
                                        final long aProdukteInWarenkorb) {
       WKorbProduktTimeKeeper.startTimeWKorbProdukt();
       strategy = injector.getInstance(WKorbProduktGenerator.class);
       strategy.generateProdukteInWKorb(aBestellzeilen, aProdukte,
                                        aProdukteInWarenkorb);
       WKorbProduktTimeKeeper.endTime();
    };
 
    protected final Creator getCreator() {
       return creator;
    }
 
    protected final void setCreator(final Creator aCreator) {
       this.creator = aCreator;
    }
 
    protected final Product getProduct() {
       return product;
    }
 
    protected final void setProduct(final Product aProduct) {
       this.product = aProduct;
    }
 
    protected final void setDBWriter(final IDBWriter aDBWriter) {
       this.dbWriter = aDBWriter;
    }
 
    protected final IDBWriter getDBWriter() {
       return dbWriter;
    }
 }
