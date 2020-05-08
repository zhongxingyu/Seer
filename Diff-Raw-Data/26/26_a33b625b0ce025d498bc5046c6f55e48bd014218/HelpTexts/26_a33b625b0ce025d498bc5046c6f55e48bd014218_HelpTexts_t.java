 package no.knubo.accounting.client;
 
 /**
  * Interface to represent the constants contained in resource bundle:
  * 	'/Users/knuterikborgen/kode/workspace/RegnskapClient/src/no/knubo/accounting/client/HelpTexts.properties'.
  */
 public interface HelpTexts extends com.google.gwt.i18n.client.ConstantsWithLookup {
   
   /**
    * Translated "Kj&oslash;rer rapport for gitt &aring;r. Inngis ikke m&aring;ned rapporteres status for hele &aring;ret.".
    * 
    * @return translated "Kj&oslash;rer rapport for gitt &aring;r. Inngis ikke m&aring;ned rapporteres status for hele &aring;ret."
    */
   @DefaultStringValue("Kj&oslash;rer rapport for gitt &aring;r. Inngis ikke m&aring;ned rapporteres status for hele &aring;ret.")
   @Key("GeneralReport_reportButton")
   String GeneralReport_reportButton();
 
   /**
    * Translated "Lagrer posteringen.".
    * 
    * @return translated "Lagrer posteringen."
    */
   @DefaultStringValue("Lagrer posteringen.")
   @Key("RegisterHappening_saveButton")
   String RegisterHappening_saveButton();
 
   /**
    * Translated "Bel&oslash;p for posteringen.".
    * 
    * @return translated "Bel&oslash;p for posteringen."
    */
   @DefaultStringValue("Bel&oslash;p for posteringen.")
   @Key("amount")
   String amount();
 
   /**
    * Translated "Bilag er bilagsnummeret til posteringen. Bilagsnummer er p&aring;krevd, men m&aring; ikke v&aelig;re unik per postering. God regnskapsskikk tilsier at alle posteringer skal v&aelig;re underst&oslash;ttet av et bilag, hvor bilaget skal inneholde orginalkvitteringer attestert av Leder og Kasserer.".
    * 
    * @return translated "Bilag er bilagsnummeret til posteringen. Bilagsnummer er p&aring;krevd, men m&aring; ikke v&aelig;re unik per postering. God regnskapsskikk tilsier at alle posteringer skal v&aelig;re underst&oslash;ttet av et bilag, hvor bilaget skal inneholde orginalkvitteringer attestert av Leder og Kasserer."
    */
   @DefaultStringValue("Bilag er bilagsnummeret til posteringen. Bilagsnummer er p&aring;krevd, men m&aring; ikke v&aelig;re unik per postering. God regnskapsskikk tilsier at alle posteringer skal v&aelig;re underst&oslash;ttet av et bilag, hvor bilaget skal inneholde orginalkvitteringer attestert av Leder og Kasserer.")
   @Key("attachment")
   String attachment();
 
   /**
    * Translated "Beskrivelse av eiendelen.".
    * 
    * @return translated "Beskrivelse av eiendelen."
    */
   @DefaultStringValue("Beskrivelse av eiendelen.")
   @Key("belonging_description")
   String belonging_description();
 
   /**
    * Translated "Nullstiller felter".
    * 
    * @return translated "Nullstiller felter"
    */
   @DefaultStringValue("Nullstiller felter")
   @Key("clear")
   String clear();
 
   /**
    * Translated "Dagen posteringen skal registreres p&aring; i den aktive m&aring;neden.".
    * 
    * @return translated "Dagen posteringen skal registreres p&aring; i den aktive m&aring;neden."
    */
   @DefaultStringValue("Dagen posteringen skal registreres p&aring; i den aktive m&aring;neden.")
   @Key("day")
   String day();
 
   /**
    * Translated "Dagen posteringen skal registreres p&aring;.".
    * 
    * @return translated "Dagen posteringen skal registreres p&aring;."
    */
   @DefaultStringValue("Dagen posteringen skal registreres p&aring;.")
   @Key("day_single")
   String day_single();
 
   /**
    * Translated "Standardprosjekt for registrering av bilagslinjen. N&aring;r valgt velges denne for hver ny rad.".
    * 
    * @return translated "Standardprosjekt for registrering av bilagslinjen. N&aring;r valgt velges denne for hver ny rad."
    */
   @DefaultStringValue("Standardprosjekt for registrering av bilagslinjen. N&aring;r valgt velges denne for hver ny rad.")
   @Key("default_project")
   String default_project();
 
   /**
    * Translated "Beskrivelse skal kortfattet oppsumere innholdet i posteringen.".
    * 
    * @return translated "Beskrivelse skal kortfattet oppsumere innholdet i posteringen."
    */
   @DefaultStringValue("Beskrivelse skal kortfattet oppsumere innholdet i posteringen.")
   @Key("description")
   String description();
 
   /**
    * Translated "Det kan oppgis flere epostadresser ved &aring; skille dem med komma.".
    * 
    * @return translated "Det kan oppgis flere epostadresser ved &aring; skille dem med komma."
    */
   @DefaultStringValue("Det kan oppgis flere epostadresser ved &aring; skille dem med komma.")
   @Key("email")
   String email();
 
   /**
    * Translated "Personen er skjult fra s&oslash;k.".
    * 
    * @return translated "Personen er skjult fra s&oslash;k."
    */
   @DefaultStringValue("Personen er skjult fra s&oslash;k.")
   @Key("hidden_person")
   String hidden_person();
 
   /**
    * Translated "En bruker kan settes til &aring; alltid m&aring;tte registrere regnskap p&aring; et prosjekt.".
    * 
    * @return translated "En bruker kan settes til &aring; alltid m&aring;tte registrere regnskap p&aring; et prosjekt."
    */
   @DefaultStringValue("En bruker kan settes til &aring; alltid m&aring;tte registrere regnskap p&aring; et prosjekt.")
   @Key("hint0")
   String hint0();
 
   /**
    * Translated "En person kan ha flere epostadresser, separert med komma.".
    * 
    * @return translated "En person kan ha flere epostadresser, separert med komma."
    */
   @DefaultStringValue("En person kan ha flere epostadresser, separert med komma.")
   @Key("hint1")
   String hint1();
 
   /**
    * Translated "Se detaljer p&aring; en postering ved &aring; klikke p&aring; bilagsbeskrivelsen i m&aring;nedsoversikten".
    * 
    * @return translated "Se detaljer p&aring; en postering ved &aring; klikke p&aring; bilagsbeskrivelsen i m&aring;nedsoversikten"
    */
   @DefaultStringValue("Se detaljer p&aring; en postering ved &aring; klikke p&aring; bilagsbeskrivelsen i m&aring;nedsoversikten")
   @Key("hint10")
   String hint10();
 
   /**
    * Translated "F&oslash;rste bokstav p&aring; knapper er alltid akselerator (ctrl+tegn)".
    * 
    * @return translated "F&oslash;rste bokstav p&aring; knapper er alltid akselerator (ctrl+tegn)"
    */
   @DefaultStringValue("F&oslash;rste bokstav p&aring; knapper er alltid akselerator (ctrl+tegn)")
   @Key("hint11")
   String hint11();
 
   /**
    * Translated "I Info menyen finner du kalkulator. ".
    * 
    * @return translated "I Info menyen finner du kalkulator. "
    */
   @DefaultStringValue("I Info menyen finner du kalkulator. ")
   @Key("hint12")
   String hint12();
 
   /**
    * Translated "Du kan endre bilagsnummer under registrering og p&aring; den m&aring;ten registrere flere rader p&aring; samme bilag.".
    * 
    * @return translated "Du kan endre bilagsnummer under registrering og p&aring; den m&aring;ten registrere flere rader p&aring; samme bilag."
    */
   @DefaultStringValue("Du kan endre bilagsnummer under registrering og p&aring; den m&aring;ten registrere flere rader p&aring; samme bilag.")
   @Key("hint2")
   String hint2();
 
   /**
    * Translated "Du kan sette profilinnstillling p&aring; bruker slik at de m&aring; f&oslash;re p&aring; prosjekt. Kjekk m&aring;te &aring; delegere f&oslash;ring av et arrangement til andre.".
    * 
    * @return translated "Du kan sette profilinnstillling p&aring; bruker slik at de m&aring; f&oslash;re p&aring; prosjekt. Kjekk m&aring;te &aring; delegere f&oslash;ring av et arrangement til andre."
    */
   @DefaultStringValue("Du kan sette profilinnstillling p&aring; bruker slik at de m&aring; f&oslash;re p&aring; prosjekt. Kjekk m&aring;te &aring; delegere f&oslash;ring av et arrangement til andre.")
   @Key("hint3")
   String hint3();
 
   /**
    * Translated "Personer har fritekstfelt hvor man kan legge inn kommentarer".
    * 
    * @return translated "Personer har fritekstfelt hvor man kan legge inn kommentarer"
    */
   @DefaultStringValue("Personer har fritekstfelt hvor man kan legge inn kommentarer")
   @Key("hint4")
   String hint4();
 
   /**
    * Translated "Personer kan ha hemmelig adresse og da kan kun de brukere som har tilgang til &aring; lese denne se dem.".
    * 
    * @return translated "Personer kan ha hemmelig adresse og da kan kun de brukere som har tilgang til &aring; lese denne se dem."
    */
   @DefaultStringValue("Personer kan ha hemmelig adresse og da kan kun de brukere som har tilgang til &aring; lese denne se dem.")
   @Key("hint5")
   String hint5();
 
   /**
    * Translated "Registrer du samme enkle bilag hver m&aring;ned? Pr&oslash;v Hurtiregistrering av standardbilag.".
    * 
    * @return translated "Registrer du samme enkle bilag hver m&aring;ned? Pr&oslash;v Hurtiregistrering av standardbilag."
    */
   @DefaultStringValue("Registrer du samme enkle bilag hver m&aring;ned? Pr&oslash;v Hurtiregistrering av standardbilag.")
   @Key("hint6")
   String hint6();
 
   /**
    * Translated "Vis detaljer for utvalgte posteringer kan s&oslash;ke per prosjekt og kan summere resultat per konti.".
    * 
    * @return translated "Vis detaljer for utvalgte posteringer kan s&oslash;ke per prosjekt og kan summere resultat per konti."
    */
   @DefaultStringValue("Vis detaljer for utvalgte posteringer kan s&oslash;ke per prosjekt og kan summere resultat per konti.")
   @Key("hint7")
   String hint7();
 
   /**
    * Translated "Systemet har ferdig rapport til NIF for kj&oslash;nn og aldersfordeling".
    * 
    * @return translated "Systemet har ferdig rapport til NIF for kj&oslash;nn og aldersfordeling"
    */
   @DefaultStringValue("Systemet har ferdig rapport til NIF for kj&oslash;nn og aldersfordeling")
   @Key("hint8")
   String hint8();
 
   /**
    * Translated "Benytt budsjett til &aring; holde kontroll p&aring; &oslash;konomien".
    * 
    * @return translated "Benytt budsjett til &aring; holde kontroll p&aring; &oslash;konomien"
    */
   @DefaultStringValue("Benytt budsjett til &aring; holde kontroll p&aring; &oslash;konomien")
   @Key("hint9")
   String hint9();
 
   /**
    * Translated "Serienummeret skal unikt identifisere eiendelen slik at det blir enklere &aring; holde oversikt over eiendeler av samme type.".
    * 
    * @return translated "Serienummeret skal unikt identifisere eiendelen slik at det blir enklere &aring; holde oversikt over eiendeler av samme type."
    */
   @DefaultStringValue("Serienummeret skal unikt identifisere eiendelen slik at det blir enklere &aring; holde oversikt over eiendeler av samme type.")
   @Key("identifier")
   String identifier();
 
   /**
    * Translated "<p>Det mangler pris for kursmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>".
    * 
    * @return translated "<p>Det mangler pris for kursmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>"
    */
   @DefaultStringValue("<p>Det mangler pris for kursmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>")
   @Key("missing_course_price")
   String missing_course_price();
 
   /**
    * Translated "<p>Det mangler pris for junior-&aring;rsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>".
    * 
    * @return translated "<p>Det mangler pris for junior-&aring;rsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>"
    */
   @DefaultStringValue("<p>Det mangler pris for junior-&aring;rsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>")
   @Key("missing_member_price")
   String missing_member_price();
 
   /**
    * Translated "<p>Det mangler pris for &aring;rsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>".
    * 
    * @return translated "<p>Det mangler pris for &aring;rsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>"
    */
   @DefaultStringValue("<p>Det mangler pris for &aring;rsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>")
   @Key("missing_member_youth_price")
   String missing_member_youth_price();
 
   /**
   * Translated "Klarte ikke finne neste semester. Operasjonen er avbrutt.".
   * 
   * @return translated "Klarte ikke finne neste semester. Operasjonen er avbrutt."
   */
  @DefaultStringValue("Klarte ikke finne neste semester. Operasjonen er avbrutt.")
  @Key("missing_semester")
  String missing_semester();

  /**
    * Translated "<p>Det mangler pris for treningsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>".
    * 
    * @return translated "<p>Det mangler pris for treningsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>"
    */
   @DefaultStringValue("<p>Det mangler pris for treningsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>")
   @Key("missing_train_price")
   String missing_train_price();
 
   /**
    * Translated "<p>Det mangler pris for ungdomsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>".
    * 
    * @return translated "<p>Det mangler pris for ungdomsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>"
    */
   @DefaultStringValue("<p>Det mangler pris for ungdomsmedlemsskap for i &aring;r. Kan ikke fullf&oslash;re registreringen.</p><p>Legg inn pris i innstillinger, Priser for medlemsskap.</p>")
   @Key("missing_youth_price")
   String missing_youth_price();
 
   /**
    * Translated "I feltene under kan bel&oslash;pet inngis ved &aring; gi inn antall av gitt val&oslash;r.".
    * 
    * @return translated "I feltene under kan bel&oslash;pet inngis ved &aring; gi inn antall av gitt val&oslash;r."
    */
   @DefaultStringValue("I feltene under kan bel&oslash;pet inngis ved &aring; gi inn antall av gitt val&oslash;r.")
   @Key("money_type")
   String money_type();
 
   /**
    * Translated "M&aring;ned - mm, for eksempel 12.".
    * 
    * @return translated "M&aring;ned - mm, for eksempel 12."
    */
   @DefaultStringValue("M&aring;ned - mm, for eksempel 12.")
   @Key("month")
   String month();
 
   /**
    * Translated "Antall femti&oslash;rer.".
    * 
    * @return translated "Antall femti&oslash;rer."
    */
   @DefaultStringValue("Antall femti&oslash;rer.")
   @Key("number0.5")
   String number0_5();
 
   /**
    * Translated "Antall kronestykker.".
    * 
    * @return translated "Antall kronestykker."
    */
   @DefaultStringValue("Antall kronestykker.")
   @Key("number1")
   String number1();
 
   /**
    * Translated "Antall tiere.".
    * 
    * @return translated "Antall tiere."
    */
   @DefaultStringValue("Antall tiere.")
   @Key("number10")
   String number10();
 
   /**
    * Translated "Antall hundrelapper.".
    * 
    * @return translated "Antall hundrelapper."
    */
   @DefaultStringValue("Antall hundrelapper.")
   @Key("number100")
   String number100();
 
   /**
    * Translated "Antall tusenlapper.".
    * 
    * @return translated "Antall tusenlapper."
    */
   @DefaultStringValue("Antall tusenlapper.")
   @Key("number1000")
   String number1000();
 
   /**
    * Translated "Antall tyvekroner.".
    * 
    * @return translated "Antall tyvekroner."
    */
   @DefaultStringValue("Antall tyvekroner.")
   @Key("number20")
   String number20();
 
   /**
    * Translated "Antall tohundrelapper.".
    * 
    * @return translated "Antall tohundrelapper."
    */
   @DefaultStringValue("Antall tohundrelapper.")
   @Key("number200")
   String number200();
 
   /**
    * Translated "Antall femmere.".
    * 
    * @return translated "Antall femmere."
    */
   @DefaultStringValue("Antall femmere.")
   @Key("number5")
   String number5();
 
   /**
    * Translated "Antall femtilapper.".
    * 
    * @return translated "Antall femtilapper."
    */
   @DefaultStringValue("Antall femtilapper.")
   @Key("number50")
   String number50();
 
   /**
    * Translated "Antall femhundrelapper.".
    * 
    * @return translated "Antall femhundrelapper."
    */
   @DefaultStringValue("Antall femhundrelapper.")
   @Key("number500")
   String number500();
 
   /**
    * Translated "Postnr angir sorteringen som posteringen f&aring;r i den aktive m&aring;neden. Postnummeret tildeles automatisk og det er ikke behov for &aring; endre denne med mindre rekkef&oslash;lgen p&aring; posteringen m&aring; endres.".
    * 
    * @return translated "Postnr angir sorteringen som posteringen f&aring;r i den aktive m&aring;neden. Postnummeret tildeles automatisk og det er ikke behov for &aring; endre denne med mindre rekkef&oslash;lgen p&aring; posteringen m&aring; endres."
    */
   @DefaultStringValue("Postnr angir sorteringen som posteringen f&aring;r i den aktive m&aring;neden. Postnummeret tildeles automatisk og det er ikke behov for &aring; endre denne med mindre rekkef&oslash;lgen p&aring; posteringen m&aring; endres.")
   @Key("postnmb")
   String postnmb();
 
   /**
    * Translated "F&oslash;res p&aring; post angir hvilken hurtiregistrering som skal posteres. Ved lagring vil det alltid opprettes en debet post og en kredit post med bel&oslash;pet som inngis.".
    * 
    * @return translated "F&oslash;res p&aring; post angir hvilken hurtiregistrering som skal posteres. Ved lagring vil det alltid opprettes en debet post og en kredit post med bel&oslash;pet som inngis."
    */
   @DefaultStringValue("F&oslash;res p&aring; post angir hvilken hurtiregistrering som skal posteres. Ved lagring vil det alltid opprettes en debet post og en kredit post med bel&oslash;pet som inngis.")
   @Key("register_count_post")
   String register_count_post();
 
   /**
    * Translated "Utf&oslash;rer s&oslash;k.".
    * 
    * @return translated "Utf&oslash;rer s&oslash;k."
    */
   @DefaultStringValue("Utf&oslash;rer s&oslash;k.")
   @Key("search")
   String search();
 
   /**
    * Translated "Avbryter registreringen og lukker vinudet.".
    * 
    * @return translated "Avbryter registreringen og lukker vinudet."
    */
   @DefaultStringValue("Avbryter registreringen og lukker vinudet.")
   @Key("trustStatusView_cancelButton")
   String trustStatusView_cancelButton();
 
   /**
    * Translated "&Aring;pne vindu for registrering av fondsaktivitet.".
    * 
    * @return translated "&Aring;pne vindu for registrering av fondsaktivitet."
    */
   @DefaultStringValue("&Aring;pne vindu for registrering av fondsaktivitet.")
   @Key("trustStatusView_newTrustButton")
   String trustStatusView_newTrustButton();
 
   /**
    * Translated "&Aring;r - yyyy, for eksempel 2009.".
    * 
    * @return translated "&Aring;r - yyyy, for eksempel 2009."
    */
   @DefaultStringValue("&Aring;r - yyyy, for eksempel 2009.")
   @Key("year")
   String year();
 }
