 /**
  * 
  */
 package de.fhhof.streckenliste.reporting;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import de.fhhof.streckenliste.reporting.DataIO;
 import de.fhhof.streckenliste.reporting.daten.*;
 
 /**
  * @author ronny
 * 
  *@deprecated
  */
 public class DataFileIO implements DataIO {
 	/* (non-Javadoc)
 	 * @see de.fhhof.streckenliste.reporting.DataFileIO#readStreckenliste()
 	 * gibt ein Streckenlistenobjekt  zurück sollten beim lesen oder parsen Fehler auftreten werden diese behandelt und ein leeres Streckenlistenobjekt zurückgegeben
 	 * gibt nur letztes Jagt jahr zurück
 	 */ 
 	public  Streckenliste readStreckenliste() {
 		Document doc = new Document();
 		int maxJagtJahr=0;// um letztes Jahr zu bestimmen
 		String xmlFile="StreckenlistePC_V4.xml";
 		try{
 			doc = (new SAXBuilder()).build(xmlFile);
 		}
 		catch (Exception e)
 		{
 			System.out.println("Fehler beim lesen von"+xmlFile+"\n");
 		}
 		ListeA lista = null;
 		ListeB listb=null;
 		Element root=doc.getRootElement();
 		Format form=new XMLOutputter().getFormat().getPrettyFormat();
 		//begin Anschrift
 		Element anschrUJB=root.getChild("deckblatt").getChild("anschrUJB");
 		AnschrUJB AnschrUJB=new AnschrUJB(anschrUJB.getChildText("ujbLRA"),anschrUJB.getChildText("ujbSG"),anschrUJB.getChildText("ujbStr"),anschrUJB.getChildText("ujbPLZ"),anschrUJB.getChildText("ujbOrt"));
 		//begin Revierart
 		Element revArt= root.getChild("deckblatt").getChild("revArt");
 		RevArt revart=RevArt.getRevArtByID(Integer.parseInt(revArt.getText()));
 		//begin Revier Name
 		Element RevName=root.getChild("deckblatt").getChild("revName");
 		String revName=RevName.getText();
 		//begin Verwertungsart
 		Element Verwerte=root.getChild("deckblatt").getChild("verwert");
 		Verwert verwert=Verwert.getVerwertByID(Integer.parseInt(Verwerte.getText()));
 		//begin bSatzart
 		String bSatzart=root.getChild("deckblatt").getChild("bSatzart").getText();
 		//begin amtID
 		String amtID=root.getChild("deckblatt").getChild("amtID").getText();
 		//begin revNr
 		String revNr=root.getChild("deckblatt").getChild("revNr").getText();
 		/*
 		 * begin Liste A
 		 */
 		Element liste_a =root.getChild("daten").getChild("listeA");
 		List  <Element>JJahr=liste_a.getChildren();
 		String year =""+GregorianCalendar.getInstance().get(1);
 		//Variablen für die aktuelle Streckenliste A deklarienen
 		String jJahr;
 		GregorianCalendar abgDatum=new GregorianCalendar();
 		String ort="";
 		Object abgUnterschrift="";
 		Vector<AZeile> aZeileV=new Vector();;
 		HashMap<AKlasse, Integer> sumErl = new HashMap();
 		HashMap<AKlasse, Integer> sumFall= new HashMap();
 		HashMap<AKlasse, Integer> sumGes= new HashMap();
 		// füllen der HashMaps
 		for (int i=0;i<200;i++)
 		{
 			if (AKlasse.getAKlasseByID(i)!=null)
 			{
 				sumErl.put(AKlasse.getAKlasseByID(i), (Integer)(0));
 				sumFall.put(AKlasse.getAKlasseByID(i), (Integer) 0);
 				sumGes.put(AKlasse.getAKlasseByID(i),  (Integer)0);
 			}
 		}
 		//letztes Jagtjahr holen
 		//Format für Datum angeben
 		DateFormat df=new SimpleDateFormat("dd.MM.yyyy");
 		for (Element o:JJahr)
 		{
 			if(Integer.parseInt(o.getAttributeValue("jJahr"))>maxJagtJahr)
 				maxJagtJahr=Integer.parseInt(o.getAttributeValue("jJahr"));
 		}
 		//letztes Jagt Jahr parsen
 		for (Element o:JJahr)
 		{
 			if(Integer.parseInt(o.getAttributeValue("jJahr"))==maxJagtJahr)
 			{
 				jJahr=""+maxJagtJahr;
 				try {
 					abgDatum.setTime(df.parse(o.getChildText("abgDatum")));
 				} catch (ParseException e) {
 					System.out.println("Fehler beim parsen von abgDatum im Jahr "+maxJagtJahr);
 					e.printStackTrace();
 				}
 				ort=o.getChildText("ort");
 				abgUnterschrift=o.getChildText("abgUschrift");
 				List <Element> aZeilea=o.getChildren("aZeile");
 
 				for(Element e:aZeilea)
 				{
 					GregorianCalendar aDate=new GregorianCalendar();
 					GregorianCalendar aMeld=new GregorianCalendar();
 					int r=0;//aErlegt(1), aVerkehr(2), aSonstFall(3), aGefangen(4)
 					try{
 						aDate.setTimeInMillis(df.parse(e.getChild("aDatum").getText()).getTime());
 
 						try
 						{
 							aMeld.setTimeInMillis(df.parse(e.getChild("aMeldedatum").getText()).getTime());
 						}
 						catch (Exception err)
 						{
 							aMeld=null;
 						}
 
 
 						//sumErl; sumFall;sumGes;
 						if (e.getChildText("aaZeileArt")!=null)
 						{
 							if (e.getChildText("aaZeileArt").equalsIgnoreCase("Erlegt"))
 							{
 								r=1;
 								try
 								{
 									sumErl.put(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))), (Integer)sumErl.get(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))))+1);
 									//	System.out.println(sumErl.get(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))))+1);
 								}
 								catch (Exception err)
 								{
 									System.out.println("**********************");
 									System.out.println("err sumErl");
 									System.out.println("aKlasse="+AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))));
 									System.out.println("Klasse="+e.getChildText("aKlasse"));
 									System.out.println("sumErl:"+(Integer)sumErl.get(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse")))));
 									System.out.println("****************");
 
 								}
 							}
 							else if (e.getChildText("aaZeileArt").equalsIgnoreCase("Verkehr")||e.getChildText("aaZeileArt").equalsIgnoreCase("sonstFallwild"))
 							{
 								r=2;
 								if(e.getChildText("aaZeileArt").equalsIgnoreCase("sonstFallwild"))
 									r=3;
 								try
 								{
 									sumFall.put(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))), (Integer)sumFall.get(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))))+1);
 								}
 								catch (Exception err)
 								{
 									System.out.println("**********************");
 									System.out.println("err sumFall");
 									System.out.println("aKlasse:"+AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))));
 									System.out.println("**********************");
 								}
 							}
 						}
 						try{
 							sumGes.put(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))), (Integer)sumGes.get(AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))))+1);
 						}
 
 						catch (Exception err)
 						{
 							System.out.println("err sumGes");
 						}}
 					catch (Exception err)
 					{
 						System.out.println("Err parsing Azeile Date");
 					}
 					if (e.getChildText("aaZeileArt").equalsIgnoreCase("Gefangen"))
 						r=4;
 					//da aGewicht mit Komma angegeben ist muss mit einem Formatierer gearbeitet werden
 					Float aGwe=0F;
 					try
 					{
 						DecimalFormatSymbols nf=new DecimalFormatSymbols();
 						nf.setDecimalSeparator(',');
 						DecimalFormat format=new DecimalFormat();
 						format.setDecimalFormatSymbols(nf);
 						aGwe=(format.parse(e.getChildText("aGewicht"))).floatValue();
 					}
 					catch (Exception err)
 					{
 						System.out.println("Fehler beim umwandeln von aGewicht in Float");
 					}
 
 					try
 					{
 						aZeileV.add(new AZeile(
 								aDate,
 								aGwe,
 								AWildart.getAWildartByID(Integer.parseInt(e.getChildText("aWildart"))),
 								AKlasse.getAKlasseByID(Integer.parseInt(e.getChildText("aKlasse"))),
 								""+e.getChildText("aBemerk"),AEintragArt.getAEintragArtByID(r),
 								aMeld,
 								Integer.parseInt(e.getAttributeValue("aID"))));
 					}
 					catch (Exception err)
 					{
 						System.out.println("err in add aZeile Vector");
 						err.getStackTrace();
 					}
 				}
 				lista=new ListeA(jJahr, abgDatum,  ort, abgUnterschrift, aZeileV, sumErl, sumFall, sumGes);
 				/* 
 				 * Ende Liste a
 				 * 
 				 * 
 				 *Anfang ListeB 
 				 */
 			}}
 		Element liste_b =root.getChild("daten").getChild("listeB");
 		List<Element> jJahrb= liste_b.getChildren();
 		Vector<BZeile> bZeile = new Vector<BZeile>();
 		GregorianCalendar jgt=new GregorianCalendar();
 		jgt.set(1,maxJagtJahr);
 		for(Element b :jJahrb)
 		{
 			if(Integer.parseInt(b.getAttribute("jJahr").getValue())==maxJagtJahr)// das Jagtjahr wird aus Liste A geholt maxValue
 			{
 				
 				List<Element>bz =b.getChildren("bZeile");
 				for(Element tx:bz)
 				{
 				int anzErl=0;
 				int anzVerend=0;
 				int anzVerk=0;
 				String bemerk="";
 				try
 				{
 					anzErl=Integer.parseInt(tx.getChildText("bAnzErlegt"));
 				}
 				catch (Exception err)
 				{
 					//kann bAnzErlegt nicht parsen
 				}
 				try 
 				{
 					anzVerend=Integer.parseInt(tx.getChildText("bAnzFallVerend"));
 				}
 				catch (Exception err)
 				{
 					//kann bAnzFallVerend nicht parsen
 				}
 				try 
 				{
 					anzVerend=Integer.parseInt(tx.getChildText("bAnzFallVerkehr"));
 				}
 				catch (Exception err)
 				{
 					//kann bAnzFallVerkehr nicht parsen
 				}
 				try 
 				{
 					bemerk=tx.getChildText("bBemerk");
 				}
 				catch (Exception err)
 				{
 					//kann bBemerk nicht parsen
 				}
 				
 			try
 			{
 				
 				bZeile.add(new BZeile(
 				BWildart.getBWildartByID(Integer.parseInt(tx.getAttributeValue("wildartID"))),
 				anzErl,
 				anzVerend,
 				anzVerk,
 				bemerk));
 			}
 			catch (Exception err)
 			{
 				System.out.println("err in add BZeile");
 				System.out.println("Bemerkung:"+b.getChildText("bBemerk"));
 				System.out.println(b.getName());
 			}
 		}
 				listb=new ListeB(jgt);
 				listb.setbZeile(bZeile);
 				}
 		
 		}
 		Streckenliste streckenliste=new Streckenliste();
 		streckenliste.setAmtID(amtID);
 		streckenliste.setListeA(lista);
 		streckenliste.setListeB(listb);
 		return streckenliste;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.fhhof.streckenliste.reporting.DataFileIO#streckenlisteAbschliessen(int, java.lang.String)
 	 */
 	public void streckenlisteAbschliessen(int jahr, String revier) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see de.fhhof.streckenliste.reporting.DataFileIO#streckenlisteZwischenmeldung(int, java.lang.String)
 	 */
 
 	public void streckenlisteZwischenmeldung(int jahr, String revier) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public Streckenliste readStreckenliste(int jahr, String revier) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
