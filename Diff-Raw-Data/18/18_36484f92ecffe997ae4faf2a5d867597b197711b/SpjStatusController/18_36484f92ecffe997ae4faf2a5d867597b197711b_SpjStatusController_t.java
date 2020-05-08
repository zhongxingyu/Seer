 package spj.controller;
 
 import com.google.common.collect.ImmutableList;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 import spj.database.SpjDomainMap;
 import spj.database.SpjDomainModelEntity;
 import spj.database.aktivnost.AktivnostEntity;
 import spj.database.izvorfinanciranja.IzvorFinanciranjaEntity;
 import spj.database.program.ProgramEntity;
 import spj.shared.domain.*;
 import spj.shared.remoting.service.SpjRemotingService;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.Date;
 import java.util.List;
 
 @Controller
 public class SpjStatusController
 {
     @Autowired
     SpjRemotingService spjRemotingService;
 
     @PersistenceContext
     EntityManager entityManager;
 
     @RequestMapping("/")
     public ResponseEntity<String> status()
     {
         return new ResponseEntity<String>("SPJ working!", HttpStatus.OK);
     }
 
     @RequestMapping("/test")
     public ResponseEntity<String> test()
     {
         try
         {
             testMjesto();
             testPartner();
             testMaticniPodaci();
             testFunkcija();
             testRazdjel();
             testGlava();
             testKorisnikProracuna();
             testOrganizacijskaJedinica();
             testIzvorFinanciranja();
             testKonto();
             testIzvod();
             testGlavniProgram();
             testProgram();
             testAktivnost();
             return new ResponseEntity<String>("Splendid!", HttpStatus.OK);
 
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
             return new ResponseEntity<String>("Something went terribly wrong! <br/><br/>" + exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
         }
         finally
         {
             try
             {
                 cleanAll();
             }
             catch (Exception exception)
             {
                 exception.printStackTrace();
                 return new ResponseEntity<String>("Something went terribly wrong! <br/><br/>" + exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
             }
         }
     }
 
     @RequestMapping("/clean")
     public ResponseEntity<String> clean()
     {
         try
         {
             cleanAll();
 
             return new ResponseEntity<String>("All is well, again!", HttpStatus.OK);
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
             return new ResponseEntity<String>("Something went terribly wrong! <br/><br/>" + exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
         }
     }
 
     private void testMjesto()
     {
         Mjesto mjesto = Mjesto.builder().id("9999").naziv("test").pozivniBroj("test").postanskiBroj("test").build();
         Mjesto updatedMjesto = Mjesto.builder(mjesto).naziv("test2").build();
         testModel(mjesto, updatedMjesto);
     }
 
     private void testPartner()
     {
         Mjesto mjesto = spjRemotingService.read(9999L, Mjesto.class);
         Partner partner = Partner.builder()
                 .id("9999")
                 .naziv("test")
                 .telefon("test")
                 .odgovornaOsoba("test")
                 .fax("test")
                 .ziroRacun("test")
                 .oib("test")
                 .email("test")
                 .adresa("test")
                 .mjesto(mjesto)
                 .build();
 
         Partner updatedPartner = Partner.builder(partner).naziv("test2").build();
         testModel(partner, updatedPartner);
     }
 
     private void testMaticniPodaci()
     {
         Mjesto mjesto = spjRemotingService.read(9999L, Mjesto.class);
         MaticniPodaci maticniPodaci = MaticniPodaci.builder()
                 .id("9999")
                 .naziv("test")
                 .telefon("test")
                 .odgovornaOsoba("test")
                 .fax("test")
                 .ziroRacun("test")
                 .oib("test")
                 .adresa("test")
                 .mjesto(mjesto)
                 .build();
 
         MaticniPodaci updatedMaticniPodaci = MaticniPodaci.builder(maticniPodaci).naziv("test2").build();
         testModel(maticniPodaci, updatedMaticniPodaci);
     }
 
     private void testFunkcija()
     {
         Funkcija funkcija = Funkcija.builder().id("9999").naziv("test").build();
         Funkcija updatedFunkcija = Funkcija.builder(funkcija).naziv("test2").build();
         testModel(funkcija, updatedFunkcija);
     }
 
     private void testRazdjel()
     {
         Razdjel razdjel = Razdjel.builder().id("999").naziv("test").build();
         Razdjel updatedRazdjel = Razdjel.builder().id("999").naziv("test2").build();
         testModel(razdjel, updatedRazdjel);
     }
 
     private void testGlava()
     {
         Razdjel razdjel = spjRemotingService.read(999L, Razdjel.class);
         Glava glava = Glava.builder().id("99").naziv("test").razdjel(razdjel).build();
         Glava updatedGlava = Glava.builder().id("99").naziv("test2").razdjel(razdjel).build();
         testModel(glava, updatedGlava);
         spjRemotingService.findAllChildren(razdjel, Glava.class);
     }
 
     private void testKorisnikProracuna()
     {
         Glava glava = spjRemotingService.read(99999L, Glava.class);
         KorisnikProracuna korisnikProracuna = KorisnikProracuna.builder().id("99999").naziv("test").glava(glava).build();
         KorisnikProracuna updatedKorisnikProracuna = KorisnikProracuna.builder().id("99999").naziv("test2").glava(glava).build();
         testModel(korisnikProracuna, updatedKorisnikProracuna);
         spjRemotingService.findAllChildren(glava, KorisnikProracuna.class);
     }
 
     private void testOrganizacijskaJedinica()
     {
         KorisnikProracuna korisnikProracuna = spjRemotingService.read(99999L, KorisnikProracuna.class);
         OrganizacijskaJedinica organizacijskaJedinica = OrganizacijskaJedinica.builder().id("9999").naziv("test").korisnikProracuna(korisnikProracuna).build();
         OrganizacijskaJedinica updatedOrganizacijskaJedinica = OrganizacijskaJedinica.builder().id("9999").naziv("test2").korisnikProracuna(korisnikProracuna).build();
         testModel(organizacijskaJedinica, updatedOrganizacijskaJedinica);
         spjRemotingService.findAllChildren(korisnikProracuna, OrganizacijskaJedinica.class);
     }
 
     private void testIzvorFinanciranja()
     {
         IzvorFinanciranja izvorFinanciranja = IzvorFinanciranja.builder().id("99").naziv("test").build();
         IzvorFinanciranja updatedIzvorFinanciranja = IzvorFinanciranja.builder().id("99").naziv("test2").build();
         testModel(izvorFinanciranja, updatedIzvorFinanciranja);
     }
 
     private void testKonto()
     {
         Konto konto = Konto.builder().id("9999").naziv("test").imaSaldokonto(false).koristiSe(false).obveza(false).potrazivanje(true).build();
         Konto updatedKonto = Konto.builder().id("9999").naziv("test2").imaSaldokonto(false).koristiSe(false).obveza(false).potrazivanje(true).build();
         testModel(konto, updatedKonto);
     }
 
     private void testIzvod()
     {
         Konto konto = spjRemotingService.read(9999L, Konto.class);
         Izvod izvod = Izvod.builder()
                 .id("9999")
                 .datum(new Date())
                 .iznos(0L)
                 .opis("test")
                 .konto(konto)
                 .brojIzvoda(0)
                 .zakljucen(false)
                 .build();
 
         Izvod updatedIzvod = Izvod.builder()
                 .id("9999")
                 .datum(new Date())
                 .iznos(0L)
                 .opis("test")
                 .konto(konto)
                 .brojIzvoda(0)
                 .zakljucen(false)
                 .build();
 
         testModel(izvod, updatedIzvod);
         spjRemotingService.findAllChildren(konto, Izvod.class);
 
     }
 
     private void testGlavniProgram()
     {
         GlavniProgram glavniProgram = GlavniProgram.builder().id("J99").naziv("test").build();
         GlavniProgram updatedGlavniProgram = GlavniProgram.builder().id("J99").naziv("test2").build();
         testModel(glavniProgram, updatedGlavniProgram);
     }
 
     private void testProgram()
     {
         GlavniProgram glavniProgram = spjRemotingService.read(7499L, GlavniProgram.class);
         Program program = Program.builder()
                 .id("9999")
                 .naziv("test")
                 .zakonskaOsnova("test")
                 .brojZaposlenih("2")
                 .glavniProgram(glavniProgram)
                 .opis("test")
                 .opciCilj("test")
                 .pokazateljUspijeha("test")
                 .build();
 
        Program updatedProgram = Program.builder(program)
                .naziv("test2")
                 .build();
 
         testModel(program, updatedProgram);
         spjRemotingService.findAllChildren(glavniProgram, Program.class);
 
     }
 
     private void testAktivnost()
     {
         Program program = spjRemotingService.read(9999L, Program.class);
         List<Funkcija> funkcije = ImmutableList.of(spjRemotingService.read(9999L, Funkcija.class));
         List<Mjesto> mjesta =  ImmutableList.of(spjRemotingService.read(9999L, Mjesto.class));
         List<IzvorFinanciranja> izvoriFinanciranja = ImmutableList.of(spjRemotingService.read(99L, IzvorFinanciranja.class));
 
         Aktivnost aktivnost = Aktivnost.builder()
                 .id("A999999")
                 .naziv("test")
                 .zakonskaOsnova("test")
                 .opis("test")
                 .opciCilj("test")
                 .program(program)
                 .funkcije(funkcije)
                 .mjesta(mjesta)
                 .izvoriFinanciranja(izvoriFinanciranja)
                 .pokazateljUspijeha("test")
                 .build();
 
         Aktivnost updatedAktivnost = Aktivnost.builder(aktivnost).naziv("test2").build();
 
         testModel(aktivnost, updatedAktivnost);
     }
 
     private <T extends SpjDomainModel> void testModel(T spjDomainModel, T updatedSpjDomainModel)
     {
         spjRemotingService.create(spjDomainModel);
         spjRemotingService.update(updatedSpjDomainModel);
         spjRemotingService.read(spjDomainModel.getId(), spjDomainModel.getClass());
         spjRemotingService.count(spjDomainModel.getClass());
 
     }
 
     private <T extends SpjDomainModel> void cleanModel(Class<T> clazz, Long id)
     {
         if (spjRemotingService.exists(id, clazz))
         {
             spjRemotingService.delete(id, clazz);
         }
 
     }
 
     private void cleanAll()
     {
         cleanModel(Aktivnost.class, 65999999L);
         cleanModel(Program.class, 9999L);
         cleanModel(GlavniProgram.class, 7499L);
         cleanModel(Izvod.class, 9999L);
         cleanModel(Konto.class, 9999L);
         cleanModel(IzvorFinanciranja.class, 99L);
         cleanModel(OrganizacijskaJedinica.class, 9999L);
         cleanModel(KorisnikProracuna.class, 99999L);
         cleanModel(Glava.class, 99999L);
         cleanModel(Razdjel.class, 999L);
         cleanModel(Funkcija.class, 9999L);
         cleanModel(MaticniPodaci.class, 9999L);
         cleanModel(Partner.class, 9999L);
         cleanModel(Mjesto.class, 9999L);
     }
 
 
 }
