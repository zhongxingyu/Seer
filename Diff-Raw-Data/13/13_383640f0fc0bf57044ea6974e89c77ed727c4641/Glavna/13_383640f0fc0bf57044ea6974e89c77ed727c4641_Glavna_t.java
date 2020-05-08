 package hr.tvz.programiranje.java.glavna;
 
 import hr.tvz.programiranje.java.banka.DeviznaTransakcija;
 import hr.tvz.programiranje.java.banka.DevizniRacun;
 import hr.tvz.programiranje.java.banka.TekuciRacun;
 import hr.tvz.programiranje.java.osoba.Osoba;
 import java.math.BigDecimal;
 import java.util.Scanner;
 
 public class Glavna {
 
 	/**
 	 * @param args
 	 */
 	//main metoda 
 	public static void main(String[] args) {
 	
 		//set scanner
 		Scanner unos = new Scanner(System.in);
 		
 		//potrebno zatraziti unosenje podataka o vlasniku
 		//prvog racuna (ime, prezime i OIB) i postaviti ih u objekt klase Osoba
 		System.out.print("Unesite ime vlasnika prvog računa: ");
 		String imePrviVlasnik = unos.next();
 		System.out.print("Unesite prezime vlasnika prvog računa: ");
 		String prezimePrviVlasnik = unos.next();
 		System.out.print("Unesite OIB vlasnika prvog računa: ");
 		String oibPrviVlasnik = unos.next();
		//ponoviti unos u slučaju nije 11-znamenkasti broj
				while (!oibPrviVlasnik.matches("\\d{11}")) {
					System.err.print("Pogrešan unos. OIB mora imati 11 znamenki.\n");
					System.err.print("Ponovo unesite OIB vlasnika prvog računa: ");
					oibPrviVlasnik = unos.next();
				}
 		System.out.print("Unesite broj prvog računa: " );
 		String brojRacunaPrvogVlasnika = unos.next();
 		//Nakon toga potrebno je zatraziti unos iznosa na prvom racunu
 		System.out.print("Unesite stanje prvog računa (KN): ");
 		BigDecimal iznosPrvogVlasnikaRacuna = unos.nextBigDecimal();
 		
 		//postaviti ih u objekt klase Osoba
 		Osoba vlasnikPrvogRacuna = new Osoba(imePrviVlasnik, prezimePrviVlasnik, oibPrviVlasnik);
 		
 		//korak 10 - prvi racun pretvoriti u objekt klase TekuciRacun
 		TekuciRacun prviRacun = new TekuciRacun(vlasnikPrvogRacuna, iznosPrvogVlasnikaRacuna, brojRacunaPrvogVlasnika);
 		
 		//Na slican nacin kreirati i sve navedene podatke za 
 		//vlasnika drugog racuna i iznos za drugi racun
 		System.out.print("\n\nUnesite ime vlasnika drugog računa: ");
 		String imeDrugiVlasnik = unos.next();
 		System.out.print("Unesite prezime vlasnika drugog računa: ");
 		String prezimeDrugiVlasnik = unos.next();
 		System.out.print("Unesite OIB vlasnika drugog računa: ");
 		String oibDrugiVlasnik = unos.next();
		//ponoviti unos u slučaju nije 11-znamenkasti broj
				while (!oibDrugiVlasnik.matches("\\d{11}")) {
					System.err.print("Pogrešan unos. OIB mora imati 11 znamenki.\n");
					System.err.print("Ponovo unesite OIB vlasnika drugog računa: ");
					oibDrugiVlasnik = unos.next();
				}
 		System.out.print("Unesite IBAN drugog računa: ");
 		String ibanDrugiVlasnik = unos.next();
 		System.out.print("Unesite valutu drugog računa: ");
 		String valutaDrugiVlasnik = unos.next();
 		Osoba vlasnikDrugogRacuna = new Osoba(imeDrugiVlasnik, prezimeDrugiVlasnik, oibDrugiVlasnik);
 		System.out.print("Unesite stanje drugog računa: ");
 		BigDecimal iznosDrugogVlasnikaRacuna = unos.nextBigDecimal();
 		DevizniRacun drugiRacun = new DevizniRacun(vlasnikDrugogRacuna, iznosDrugogVlasnikaRacuna, ibanDrugiVlasnik, valutaDrugiVlasnik);
 		System.out.print("Unesite iznos transakcije (u KN) s prvog na drugi račun: ");
 		BigDecimal iznosTransakcije = unos.nextBigDecimal();
 		
 		//korak 10 - drugi racun pretvoriti u objekt klase DevizniRacun
 		DeviznaTransakcija transakcija = new DeviznaTransakcija(prviRacun, drugiRacun, iznosTransakcije);
 		transakcija.provediTransakciju();
 		
 		//Na kraju je jos¡ potrebno ispisati nova stanja na racunu
 		System.out.print("\nStanje prvog računa nakon transakcije: " + prviRacun.getStanjeRacuna() + " HRK");
 		//korak 12 - Kod ispisa novog stanja deviznog racuna potrebno je ispisivati valutu koja je
 		// postavljena na drugi racun.
 		System.out.print("\nStanje drugog računa nakon transakcije: " + drugiRacun.getStanjeRacuna() + " " + valutaDrugiVlasnik);
 		
 		//zatvori skener
 		unos.close();
 	
 	}
 	
 }
