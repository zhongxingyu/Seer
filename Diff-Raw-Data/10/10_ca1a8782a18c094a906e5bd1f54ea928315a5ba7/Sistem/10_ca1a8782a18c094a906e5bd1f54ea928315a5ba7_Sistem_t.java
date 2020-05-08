 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.sound.midi.SysexMessage;
 
 
 public class Sistem {
 	private List<Otel> otelListesi = new ArrayList<Otel>();
 	private Otel aktifOtel;
 	private Resepsiyonist aktifResepsiyonist;
 	private Kiralama kiralama;
 	private Scanner in = new Scanner(System.in);
 	
 	public Musteri musteriKaydiIsleminiBaslat() {
 		return aktifOtel.musteriKaydiIsleminiBaslat();
 
 	}
 	
 	public void musteriKayitBilgileriniGonder(Musteri m, String musteriAdi, String musteriSoyadi, 
 			long __id, short yas) {
 		aktifOtel.musteriKayitBilgileriniGonder(m, musteriAdi, musteriSoyadi, __id, yas);
 	}
 	
 	public List<Oda> odaKiralamaIsleminiBaslat(long musteriTcKimlikNo) {
 		Musteri musteri = aktifOtel.musteriGetir(musteriTcKimlikNo);
 		this.kiralama = new Kiralama(musteri);
 		return aktifOtel.uygunOdalariGetir(musteri);
 
 	}
 	
 	public List<OdaKatalogu> kataloglariGetir(){
 		return this.aktifOtel.kataloglariGetir();
 	}
 	
 	public void katalogSec(int _katalogID,Oda oda){
 		this.aktifOtel.katalogSec(_katalogID,oda);
 	}
 	
 	public void uygunOdaSec(int odaID) {
 		aktifOtel.uygunOdaSec(odaID, kiralama);
 	}
 	
 	public float kiralamaTarihiSec(int girisGun, int girisAy, int girisYil,
 								int cikisGun, int cikisAy, int cikisYil) {
 
 		float tutar = aktifOtel.kiralamaTarihiSec(new GregorianCalendar(girisYil, girisAy, girisGun), 
 				new GregorianCalendar(cikisYil, cikisAy, cikisGun), kiralama);
 		return tutar;
 		
 
 	}
 	
 	public void kiralamaSonlandir() {
 		aktifOtel.kiralamaSonlandir(kiralama);
 
 	}
 	
 	public Oda odaEklemeIsleminiBaslat() {
 		return aktifOtel.odaYarat();
 	}
 	
 	
 	public void odaOzellikleriBelirt(Oda o, int odaNo) {
 		o.odaOzellikleriBelirt(odaNo);
 	}
 	
 	public Otel otelEklemeIsleminiBaslat() {
 		Otel _otel = new Otel();
 		otelListesi.add(_otel);
 		return _otel;
 	}
 	
 	public void otelOzellikleriniGir(Otel _otel, String otelAdi, int otelID) {
 		_otel.otelOzellikleriniGir(otelAdi, otelID);
 	}
 	
 	public Resepsiyonist resepsiyonistEklemeIsleminiBaslat() {
 		return aktifOtel.resepsiyonistYarat();
 
 	}
 	
 	public void resepsiyonistOzellikleriniGir(Resepsiyonist _resepsiyonist, String ad, String soyad, int resepsiyonistID) {
 		_resepsiyonist.resepsiyonistOzellikleriniGir(ad, soyad, resepsiyonistID);
 
 	}
 	
 	public void anaMenuYazdir(){
 		System.out.println("-----TheOverLookHotel Systems-----\n" +
 				"\n1. Otele giriş." +
 				"\n2. Otel yarat." +
 				"\n0. Çıkış." +
 				"\n-----------TheOverLookHotel Systems---------Seçiminiz: ");
 	}
 	
 	public void otelYazdir() {
 		System.out.println("-------"+aktifOtel.isimGetir()+"-------\n" +
 				"\n1. Sistem'e Resepsiyonist olarak giriş yap. " +
 				"\n2. Sistem'e Yönetici olarak giriş yap." +
 				"\n\n\n0. Çıkış." +
 				"\n\n-------"+aktifOtel.isimGetir()+"-------\nSeçiminiz: ");
 	}
 	
 	private void resepsiyonistMenuYazdir() {
 		System.out.println("-------"+aktifOtel.isimGetir()+"-------\n" +
 				"\n=======Resepsiyonist: "+aktifResepsiyonist.isimGetir()+" " + aktifResepsiyonist.soyisimGetir() +"=======\n" +
 				"\n1. Müşteri Kayıt işlemi. " +
 				"\n2. Müşteri Listeleme işlemi." +
 				"\n3. Oda Kiralama İşlemi." +
 				"\n4. Boş Oda Listeleme İşlemi" +
 				"\n7. Kiralamaları Listeleme işlemi." +
 				"\n\n\n8. Otel Menü'ye geri dön." +
 				"\n9. Sistem Menü'ye geri gön." +
 				"\n0. Sistemden Çıkış." +
 				"\n=======Resepsiyonist: "+aktifResepsiyonist.isimGetir()+" " + aktifResepsiyonist.soyisimGetir() +"=======\n" +
 				"\n\n-------"+aktifOtel.isimGetir()+"-------\nSeçiminiz: ");
 
 	}
 	
 	private static void yoneticiMenuYazdir() {
 		System.out.println("-------TheOverLookHotel-------\n" +
 				"=======Yönetici Menü=======\n" +
 				"\n1. Müşteri Kayıt işlemi. " +
 				"\n2. Müşteri Listeleme işlemi." +
 				"\n3. Oda Kiralama İşlemi." +
 				"\n4. Boş Oda Listeleme İşlemi" +
 				"\n5. Yeni Oda Kayıt işlemi." +
 				"\n6. Yeni Resepsiyonist Kayıt işlemi." +
 				"\n7. Kiralamaları Listeleme işlemi." +
 				"\n10. Resepsiyonist Listeleme işlemi." +
 				"\n\n\n8. Otel Menü'ye geri dön." +
 				"\n9. Sistem Menü'ye geri gön." +
 				"\n0. Sistemden Çıkış." +
 				"\n\n-------TheOverLookHotel-------\nSeçiminiz: ");
 
 	}
 	
 	public boolean resepsiyonistIDSorgula(int id){
 		for (Resepsiyonist i: aktifOtel.getReceptionistList()){
 			if (i.resepsiyonistIDGetir() == id)
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean otelIDSorgula(int id){
 		for (Otel i: otelListesi){
 			if (i.otelIDGetir() == id)
 				return true;
 		}
 		return false;
 	}
 	
 	public void aktifOtelAyarla(Otel otel) {
 		this.aktifOtel = otel;
 		
 	}
 	
 	public Otel aktifOtelGetir(){
 		return this.aktifOtel;
 	}
 	
 	public Otel otelGetir(int id){
 		for (Otel i: otelListesi){
 			if (i.otelIDGetir() == id)
 				return i;
 		}
 		return null;
 	}
 	
 	public void aktifResepsiyonistAyarla(Resepsiyonist _resepsiyonist){
 		this.aktifResepsiyonist = _resepsiyonist;
 	}
 	
 	public Resepsiyonist resepsiyonistGetir(int id){
 		for (Resepsiyonist i: aktifOtel.getReceptionistList()){
 			if (i.resepsiyonistIDGetir() == id)
 				return i;
 		}
 		return null;
 	}
 	
 	public Kiralama kiralamaGetir(){
 		return this.kiralama;
 	}
 	
 	public void kiralamaAyarla(Kiralama _kiralama){
 		this.kiralama = _kiralama;
 	}
 	
 	public boolean menuOtelSec(Sistem sistem){
 		
 		System.out.println("Otel ID giriniz: ");
 		int gelenID = in.nextInt();
 		
 		if (!sistem.otelIDSorgula(gelenID)){
 			System.err.println("Yanlış Otel ID!...\n");
 			return false;
 			
 		}
 		 
 		else {
 			sistem.aktifOtelAyarla(sistem.otelGetir(gelenID));
 			return true;
 		}
 	}
 	
 	public boolean menuOtelYarat(Sistem sistem){
 		in.nextLine();
 		Otel _otel = sistem.otelEklemeIsleminiBaslat();
 		System.out.println("Otel Adı giriniz: ");
 		String __ad = in.nextLine();
 		System.out.println("Otel ID giriniz: ");
 		int __id = in.nextInt();
 		sistem.otelOzellikleriniGir(_otel, __ad, __id);
 		return true;
 	}
 	
 	public boolean menuResepsiyonistGirisYap(Sistem sistem){
 		System.out.println("Resepsiyonist ID giriniz: ");
 		int id = in.nextInt();
 		if (!sistem.resepsiyonistIDSorgula(id)){
 			System.err.println("Hatalı ID!...\n");
 			return false;
 			
 		}
 		else {
 			sistem.aktifResepsiyonistAyarla(sistem.resepsiyonistGetir(id));
 			return true;
 		}
 	}
 	
 	public boolean menuMusteriKayit(Sistem sistem) {
 		in.nextLine();
 		
 		Musteri __musteri = sistem.musteriKaydiIsleminiBaslat();
 		
 		System.out.println("İsim giriniz: ");
 		String __isim = in.nextLine();
 		System.out.println("Soyisim giriniz: ");
 		String __soyisim = in.nextLine();
 		System.out.println("Tc Kimlik NO giriniz: ");
 		long __id = in.nextLong();
		if (__id == 0){
			System.err.println("Tc Kimlik No yanlış.");
 			return false;
		}
		if (sistem.aktifOtelGetir().musteriGetir(__id) != null){
			System.err.println("Aynı Tc Kimlik No'ya sahip kayıtlı müşteri var.");
 			return false;
		}
 		System.out.println("Yaş giriniz: ");
 		short __yas = in.nextShort();
 		sistem.musteriKayitBilgileriniGonder(__musteri, __isim, __soyisim, __id, __yas);
 		return true;
 	}
 	
 	public boolean menuMusteriListesi(Sistem sistem){
 		for (Musteri i: sistem.aktifOtelGetir().musteriListesiGetir()){
 			System.out.println("| İsim: " + i.adGetir() + "| Soyisim: " + i.soyadGetir() 
 					+ "| Tc Kimlik: " + i.tcKimlikNoGetir() + "| Yaş: " + i.yasGetir() + " |");
 		}
 		
 		in.nextLine();
 		in.nextLine();
 		
 		return true;
 	}
 	
 	public boolean menuOdaKiralama(Sistem sistem){
 		System.out.println("Tc Kimlik No girin: ");
 		long _tcKimlikNo = in.nextLong();
 		for (Oda i: sistem.odaKiralamaIsleminiBaslat(_tcKimlikNo)){
 			System.out.println("OdaID:" + i.odaIDGetir());
 		}
 		in.nextLine();
 		System.out.println("Oda ID seçiniz: ");
 		int __odaID = in.nextInt();
 
 		sistem.uygunOdaSec(__odaID);
 		
 		System.out.println("Giris Gun: ");
 		int _girisGun = in.nextInt();
 		System.out.println("Giris Ay: ");
 		int _girisAy = in.nextInt();
 		System.out.println("Giris Yil: ");
 		int _girisYil = in.nextInt();
 		
 		System.out.println("Cikis Gun: ");
 		int _cikisGun = in.nextInt();
 		System.out.println("Cikis Ay: ");
 		int _cikisAy = in.nextInt();
 		System.out.println("Cikis Yil: ");
 		int _cikisYil = in.nextInt();
 		
 		float fiyat = sistem.kiralamaTarihiSec(_girisGun, _girisAy, _girisYil, _cikisGun, _cikisAy, _cikisYil);
 		
 		System.out.println("Fiyat: " + fiyat);
 		
 		in.nextLine();
 		
 		sistem.kiralamaSonlandir();
 		
 		return true;
 	}
 	
 	public boolean menuBosOdalar(Sistem sistem){
 		for (Oda i: sistem.aktifOtelGetir().bosOdalariGetir())
 			System.out.println("OdaID:" + i.odaIDGetir());
 		
 		in.nextLine();
 		
 		return true;
 	}
 	
 	public boolean menuKiralamaListeleme(Sistem sistem){
 		for (Kiralama i: sistem.aktifOtelGetir().kiralamaListesiGetir())
 			System.out.println("Oda: " + i.getKiralananOda().odaIDGetir() + "|Ad: " + 
 					i.getKiralayanMusteri().adGetir() + "|Giris:" + i.baslangicTarihiGetir().YEAR + ":" 
 					+ i.baslangicTarihiGetir().MONTH + ":" + i.baslangicTarihiGetir().DAY_OF_MONTH +
 					"|Cikis" + i.getBitisTarihi().YEAR + ":" + i.getBitisTarihi().MONTH + ":" +
 					i.getBitisTarihi().DAY_OF_MONTH + "Fiyat: " + i.getFiyat());
 		in.nextLine();
 		in.nextLine();
 		
 		return true;
 	}
 	
 	public boolean menuYoneticiGirisYap(Sistem sistem){
 		System.out.println("Yönetici ID giriniz: ");
 		int id = in.nextInt();
 		if (id != 237){
 			System.err.println("Hatalı ID!...\n");
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean menuOdaEkleme(Sistem sistem){
 		Oda _oda = sistem.odaEklemeIsleminiBaslat();
 		for (OdaKatalogu i: sistem.aktifOtelGetir().kataloglariGetir()){
 			System.out.println(i.katalogIDGetir()+": "+ i.isimGetir());	
 		}
 		System.out.println("\nKatalog ID seçiniz: ");
 		int __katalogID = in.nextInt();
 		sistem.aktifOtelGetir().katalogSec(__katalogID, _oda);
 		
 		System.out.println("Oda No giriniz: ");
 		int __odaNo = in.nextInt();
 		sistem.odaOzellikleriBelirt(_oda, __odaNo);
 		
 		return true;
 	}
 	
 	public boolean menuResepsiyonistEkleme(Sistem sistem){
 		Resepsiyonist __resepsiyonist = sistem.resepsiyonistEklemeIsleminiBaslat();
 		System.out.println("İsim giriniz: ");
 		String __isim = in.nextLine();
 		System.out.println("Soyisim giriniz: ");
 		String __soyisim = in.nextLine();
 		System.out.println("ID giriniz: ");
 		int __id = in.nextInt();
 		sistem.resepsiyonistOzellikleriniGir(__resepsiyonist, __isim, __soyisim, __id);
 		return true;
 	}
 	
 	public boolean menuResepsiyonistListeleme(Sistem sistem){
 		for (Resepsiyonist i: sistem.aktifOtelGetir().getReceptionistList())
 			System.out.println("Resepsiyonist Adı: " + i.isimGetir() + " Soyadi: " + i.soyisimGetir() +
 					" ID: " + i.resepsiyonistIDGetir());
 		in.nextLine();
 		
 		return true;
 	}
 	
 	
 	public static void main(String[] args) {
 		Sistem sistem = new Sistem();
 		/* Varsayılan Sınıflar */
 		Otel varsayilanOtel = new Otel("The Overlook Hotel");
 		varsayilanOtel.otelIDAyarla(237);
 		
 		Resepsiyonist varsayilanResepsiyonist = varsayilanOtel.resepsiyonistYarat();
 		varsayilanResepsiyonist.resepsiyonistOzellikleriniGir("Jack", "Torrance", 237);
 		Oda varsayilanOda = varsayilanOtel.odaYarat();
 		varsayilanOda.odaOzellikleriBelirt(237);
 		varsayilanOda.odaKataloguAyarla(new OdaKatalogu("Room 237", 237, (float) 237));
 		
 		Musteri varsayilanMusteri = varsayilanOtel.musteriKaydiIsleminiBaslat();
 		varsayilanMusteri.bilgileriKaydet("Jack", "Torrance", 237, (short) 237);
 		
 		Kiralama varsayilanKiralama = new Kiralama(varsayilanMusteri);
 		varsayilanOtel.uygunOdaSec(237, varsayilanKiralama);
 		varsayilanOtel.kiralamaTarihiSec(new GregorianCalendar(237, 2, 3), 
 				new GregorianCalendar(237, 2, 7), varsayilanKiralama);
 		
 		varsayilanOtel.kiralamaSonlandir(varsayilanKiralama);
 		
 		sistem.otelListesi.add(varsayilanOtel);
 	    
 		/* Varsayılan Sınıflar */
 	
 		
 		
 		sistem.otelListesi.add(varsayilanOtel);
 
 		Scanner in = new Scanner(System.in);
 		int gelenInt = -1;
 		String gelenStr = "";
 		while (gelenInt != 0){
 			sistem.anaMenuYazdir();
 			gelenInt = in.nextInt();
 			if (gelenInt == 1){
 				if (!sistem.menuOtelSec(sistem))
 					continue;
 			}
 			else if (gelenInt == 2){
 				if (!sistem.menuOtelYarat(sistem));
 					continue;
 			}
 			else 
 				continue;
 			
 			
 			while (gelenInt != 9 && gelenInt != 0){
 				sistem.otelYazdir();
 				gelenInt = in.nextInt();
 				if (gelenInt == 1){
 					
 					if (!sistem.menuResepsiyonistGirisYap(sistem))
 						continue;
 						
 					
 					while (gelenInt != 8 && gelenInt != 9 && gelenInt != 0){
 						sistem.resepsiyonistMenuYazdir();
 						gelenInt = in.nextInt();
 						// FIXME:
 						in.nextLine();
 						if (gelenInt == 1){
 							
 							if(!sistem.menuMusteriKayit(sistem)){
 								System.err.println("Musteri Kaydı yapılamadı.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 2){
 							if (!sistem.menuMusteriListesi(sistem)){
 								System.err.println("Liste Getirilemedi.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 3){
 							if (!sistem.menuOdaKiralama(sistem)){
 								System.err.println("Oda Kiralama Başarısız.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 4){
 							if (!sistem.menuBosOdalar(sistem)){
 								System.err.println("Boş oda listeleme başarısız.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 7){
 							
 							if (!sistem.menuKiralamaListeleme(sistem)){
 								System.err.println("Kiralama Listeleme başarısız.");
 								continue;
 							}
 							
 						}
 						
 					}
 				}
 				
 				else if (gelenInt == 2){
 					if (!sistem.menuYoneticiGirisYap(sistem)){
 						System.err.println("Yönetici girişi başarısız.");
 						continue;
 					}
 					
 					while (gelenInt != 8 && gelenInt != 9 && gelenInt != 0){
 						yoneticiMenuYazdir();
 						gelenInt = in.nextInt();
 						// FIXME:
 						in.nextLine();
 						
 						if (gelenInt == 1){
 							if (!sistem.menuMusteriKayit(sistem)){
 								System.err.println("Musteri kayıt başarısız.");
 								continue;
 							}
 							
 						}
 						
 						else if (gelenInt == 2){
 							if (!sistem.menuMusteriListesi(sistem)){
 								System.err.println("Liste Getirilemedi.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 3){
 							if (!sistem.menuOdaKiralama(sistem)){
 								System.err.println("Oda Kiralama Başarısız.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 4){
 							if (!sistem.menuBosOdalar(sistem)){
 								System.err.println("Boş oda listeleme başarısız.");
 								continue;
 							}
 						}
 						
 						else if (gelenInt == 5){
 							if (!sistem.menuOdaEkleme(sistem)){
 								System.err.println("Oda ekleme başarısız.");
 								continue;
 							}
 						}
 						else if (gelenInt == 6){
 							
 							if (!sistem.menuResepsiyonistEkleme(sistem)){
 								System.err.println("Resepsiyonist ekleme başarısız.");
 								continue;
 							}
 							
 						}
 						else if (gelenInt == 7){
 							if (!sistem.menuKiralamaListeleme(sistem)){
 								System.err.println("Kiralama Listeleme başarısız.");
 								continue;
 							}
 							
 						}
 						
 						else if (gelenInt == 10){
 							
 							if (!sistem.menuResepsiyonistListeleme(sistem)){
 								System.err.println("Resepsiyonist Listeleme Başarısız.");
 								continue;
 							}
 							
 						}
 					}
 				}
 				else if (gelenInt == 0)
 					System.exit(0);
 				
 				else {
 					System.out.println("\n\nYanlis Secim.\n\n");
 				}
 			}
 		}
 	}
 
 
 
 	
 }
