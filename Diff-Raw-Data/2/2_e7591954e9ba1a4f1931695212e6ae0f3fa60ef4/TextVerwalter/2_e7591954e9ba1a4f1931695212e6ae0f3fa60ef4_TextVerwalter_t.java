 package de.uni_hamburg.informatik.sep.zuul.spiel;
 
 
 /**
  * Zentraler Speicher der Ausgabetexte, sowie der Befehls- und Richtungstexte.
  * 
  * @author 0klein, 1jost
  *
  */
 public class TextVerwalter
 {
 	public static final String LYRISCHEREINLEITUNGSTEXT = "Willkommen zu Zuul, dem Abenteurspiel mit Dr. Little.\n\nNach einem fatalen Unfall in seinem Chemielabor ist der Wissenschaftler Dr. Little dazu verdammt, in geringer Körpergröße eine Lösung seines Dilemmas zu finden. Er weiß, dass sein Kollege Prof. Dr. Evenbigger bei seinen Forschungen über das Verhalten von Sonnenblumen unter Aussetzung von Beta-Strahlung zufälligerweise auf ein Mittel gestoßen ist, mithilfe dessen Dr. Little seine normale Größe zurückerlangen könnte." +
 			"\nProf. Dr. Evenbiggers Labor befindet sich jedoch auf der anderen Seite des Universitätscampus und in Dr. Littles aktueller körperlichen Verfassung stellt die Reise eine große Herausforderung für ihn dar. Er muss sich nun mit seiner mäßigen Orientierung das Labor des Professors finden." +
 			"\nIhnen obliegt die Verantwortung, Dr. Little sicher durch das Labyrinth von Räumen zu seinem Ziel zu bringen." +
 			"\nDabei besteht aber noch ein Problem: Dr. Little leidet stark unter dem Einfluss seines fehlgeschlagenen Experiments und die körperliche Anstrengung des Durchreisens der Räume setzt so zu, dass er nach 8 Räumen ohne Nahrungsaufnahme den qualvollen Tod erwarten muss." +
 			"\nDoch zum Glück haben unachtsame Studenten Kuchenkrümel in verschiedenen Teilen des Campus liegengelassen. Wenn es Dr. Little gelingt, einige davon zu erwischen, so verbessert sich sein Gesundheitszustand, sodass er weitere Räume durchqueren kann." +
 			"\n\nSo starten Sie nun furchtlos in ein spannendes Abenteuer und retten Sie den Doktor vor seinem Verderben.";
 	
 	public static final String EINLEITUNGSTEXT = "Willkommen zu Zuul, dem Abenteurspiel mit Dr. Little." +
 			"\n\nNach einem fatalen Unfall in seinem Chemielabor ist der Wissenschaftler Dr. Little geschrumpft. Er weiß, dass sein Kollege Prof. Dr. Evenbigger bei seinen Forschungen über das Verhalten von Sonnenblumen unter Aussetzung von Beta-Strahlung zufälligerweise auf ein Mittel gestoßen ist, mithilfe dessen Dr. Little seine normale Größe zurückerlangen könnte." +
 			"\nProf. Dr. Evenbiggers Labor befindet sich jedoch auf der anderen Seite des Universitätscampus und in Dr. Littles aktueller körperlichen Verfassung stellt die Reise eine große Herausforderung für ihn dar. Er muss sich nun mit seiner mäßigen Orientierung das Labor des Professors finden." +
 			"\nIhnen obliegt die Verantwortung, Dr. Little sicher durch das Labyrinth von Räumen zu seinem Ziel zu bringen." +
 			"\nDabei besteht aber noch ein Problem: Dr. Little leidet stark unter dem Einfluss seines fehlgeschlagenen Experiments und die körperliche Anstrengung des Durchreisens der Räume setzt so zu, dass er nach 8 Räumen ohne Nahrungsaufnahme den qualvollen Tod erwarten muss." +
 			"\nDoch zum Glück haben unachtsame Studenten Kuchenkrümel in verschiedenen Teilen des Campus liegengelassen. Wenn es Dr. Little gelingt, einige davon zu erwischen, so verbessert sich sein Gesundheitszustand, sodass er weitere Räume durchqueren kann." +
 			"\n\nSo starten Sie nun furchtlos in ein spannendes Abenteuer und retten Sie den Doktor vor seinem Verderben.";
 	
 	public static final String KUCHENIMRAUMTEXT = "In diesem Raum nimmt Dr. Little den dezent-süßen Geruch frisch verkrümelten Kuchens wahr.";
 	public static final String KUCHENGENOMMENTEXT = "Dr. Little findet einen Kuchenkrümel! Er tut ihn in seine Tasche.";
 
 	public static final String SIEGTEXT = "Gute Arbeit. Sie haben Dr. Little erfolgreich an sein Ziel gebracht. Sein Kollege Prof. Dr. Evenbigger verabreicht ihm nun das Gegenmittel und verhilft ihm wieder zu seiner vollen Größe.";
 	public static final String kuchengegessentext(int energie)
 	{
 		return "Dr. Little vernascht genüsslich einen Kuchenkrümel aus seiner Tasche. Er fühlt sich belebt und kann nun wieder "+energie+" weitere Räume bechreiten.";
 	}
 	public static final String giftkuchengegessentext(int energie)
 	{
 		return "Dr. Little isst einen Kuchenkrümel und bereut es sofort. Sein Magen krampft und er spürt, dass er nur noch "+energie+" Räume betreten können wird.";
 	}
 
 	public static final String NIEDERLAGETEXT = "Dr. Little ist vor Erschöpfung und Hunger zusammengebrochen. Starte erneut.";
 	public static final String BEENDENTEXT = "Dr. Little dankt Ihnen für Ihre Hilfe bei seinem aufregenden Abenteuer. Bis zum nächsten Mal bei Zuul.";
	public static final String HILFETEXT = "Sie haben die Hilfe aufgerufen. Ihr Ziel ist es, in möglichst wenigen Schritten das Labor von Prof. Dr. Evenbigger zu finden. Sie können nur eine bestimmte Anzahl an Räumen durchqueren. Wenn Sie den Raum wechseln, verringert sich diese Anzahl um einen Raum. Mit Krümeln kann diese Anzahl aber wieder erhöht werden, allerdings sind unter ihnen auch vergiftete Stücke. Sollten sie unterwegs eine Maus treffen, so können Sie ihr, als Gegenleistung für ein Stück Kuchen, einen Hinweis auf den Richtigen Weg entlocken. Aber auch hier können sie sich nicht sicher sein, dass der Kuchen wirklich gesund für die Maus ist... \nUm über die Texteingabe zu interagieren, stehen Ihnen folgende Befehle zur Verfügung: ";
 	public static final String RAUMWECHSELTEXT = "Anzahl betretbarer Räume: ";
 	public static final String IMMERNOCHKUCHENTEXT = "Dr. Little erahnt jedoch noch weitere Krümel in direkter Umgebung.";
 	public static final String NICHTSZUMNEHMENTEXT = "Dr. Little streckt erwartungsvoll die Hand aus, doch er greift nur nach Luft." +
 			"\nIn diesem Raum wird er nichts mehr zum Einsammeln finden.";
 	public static final String NICHTSZUMESSENTEXT = "Dr. Little sucht in seiner Tasche vergeblich nach einem Kuchenkrümel.";
 	public static final String FALSCHEEINGABE = "Ich weiß nicht, was Sie meinen...";
 	public static final String KUCHENTODTEXT = "Dr. Little ahnte nicht, dass dieser Kuchenkrümel sein letzter sein würde.\nEine fiese Chemikalie im Krümel zerstört die letzen Reste seines angegriffenen Organsystems.";
 	
 	public static final String KEINERICHTUNG = "Wohin möchten Sie gehen?";
 	public static final String KEINETUER = "Dort ist keine Tür. Wählen Sie eine andere Richtung.";
 	public static final String AUSGAENGE = "Ausgänge";
 	
 	public static final String BEFEHL_GEHEN = "gehe";
 	public static final String BEFEHL_NEHMEN = "nehmen";
 	public static final String BEFEHL_ESSEN = "essen";
 	public static final String BEFEHL_HILFE = "hilfe";
 	public static final String BEFEHL_BEENDEN = "beenden";
 	public static final String BEFEHL_GIB = "gib";
 
 	public static final String BUTTON_EINGEBEN = "enter";
 
 	public static final String RICHTUNG_NORDEN = "nord";
 	public static final String RICHTUNG_SUEDEN = "süd";
 
 	public static final String RICHTUNG_WESTEN = "west";
 	public static final String RICHTUNG_OSTEN = "ost";
 	
 	public static final String MAUS_GEFUNDEN = "Eine kleine, pelzige Maus schaut hinter der Ecke hervor. Sie schaut hungrig auf Dr. Littles Tasche.";
 	public static final String MAUS_FRAGE = "Möchten Sie der Maus einen Krümel geben, damit sie Dr. Little den Weg weist?";
 	public static final String MAUS_RICHTUNGSANGABE = "Maus: \"Ich vermute, dass %s die richtige Richtung ist.\"";
 	public static final String MAUS_KEIN_KRUEMEL = "Dr. Little hat keinen Krümel, den er abgeben könnte...";
 	public static final String MAUS_KEINE_MAUS = "Hier ist niemand, dem Dr. Little einen Krümel geben könnte...";
 
 
 }
