 package severeLobster.frontend.view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.LayoutManager;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import severeLobster.backend.spiel.ISpielfeldReadOnly;
 import severeLobster.backend.spiel.KeinStein;
 import severeLobster.backend.spiel.Spielstein;
 import severeLobster.frontend.controller.SpielfeldDarstellungsSteuerung;
 
 import infrastructure.ResourceManager;
 
 /**
  * Darstellung eines Spielfeldes mit den enthaltenen Spielsteinen. In dieser
  * Klasse wird absichtlich nichts in den AWT Thread eingereiht. Wenn von
  * ausserhalb des AWT Event Dispatch Threads Aenderungen am Zustand einer
  * Instanz dieser Klasse gemacht werden, sollten diese manuell in den AWT Thread
  * eingereiht werden (SwingUtils.invokeLater oder SwingUtils.invokeAndWait).
  * 
  * @author Lutz Kleiber
  */
 public class SpielfeldDarstellung extends JPanel {
 
     private final ResourceManager resourceManager = ResourceManager.get();
     /** Default Icon Factory fuer Spielstein Icons */
     public static final IconFactory DEFAULT_ICON_FACTORY = AdvancedDynamicallyResizingIconFactory
             .getInstance();
 
     /** Icon Factory fuer Spielstein Icons */
     private IconFactory iconFactory = SpielfeldDarstellung.DEFAULT_ICON_FACTORY;
 
     /** Beinhaltet Strategie fuer Mausklicks */
     private SpielfeldDarstellungsSteuerung spielfeldDarstellungsSteuerung;
 
     /** Darstellung der einzelnen Spielfeldzellen/Spielsteine */
     private JLabel[][] spielsteinDarstellungen;
 
     /** Anzeige der Sternanzahl in den Spalten */
     private JLabel[] sternAnzahlInSpaltenAnzeigen;
 
     /** Anzeige der Sternanzahl in den Zeilen */
     private JLabel[] sternAnzahlInZeilenAnzeigen;
 
     /**
      * Erzeugt eine neue SpielfeldDarstellung mit einer Startgroesse von 1 x 1.
      * Ruft den Konstruktor SpielfeldDarstellung(false) auf.
      */
     public SpielfeldDarstellung() {
 
         this(false);
     }
 
     /**
      * Erzeugt eine neue SpielfeldDarstellung mit einer Startgroesse von 1 x 1.
      * 
      * @param isDoubleBuffered
      *            Gibt an, ob das benutzte JPanel doppelpufferung nutzen soll,
      *            oder nicht.
      */
     public SpielfeldDarstellung(final boolean isDoubleBuffered) {
 
         super(new QuadratischeZellenGridLayout(1, 1), isDoubleBuffered);
         setOpaque(false);
         setAngezeigtesSpielfeldAbmessungen(1, 1);
     }
 
     /**
      * Stellt das uebergebe Spielfeld dar. Hierbei werden alle bisher
      * dargestellten Inhalte verworfen, die Darstellungsgroesse wird angepasst
      * und alles wird neu gezeichnet. Aktion ist aufwendig. Falls sich nur
      * einzelne Spielsteine des bisher dargestellten Spielfeldes geändert haben,
      * reichen auch die Methoden {@link setAngezeigterSpielstein} in Verbindung
      * mit {@link setAngezeigteSternAnzahlInSpalte}/
      * {@link setAngezeigteSternAnzahlInZeile}.
      * 
      * @param newSpielfeld
      *            Das neu darzustellende Spielfeld
      */
     public void setAngezeigtesSpielfeld(final ISpielfeldReadOnly newSpielfeld) {
 
         if (null == newSpielfeld) {
 
             throw new NullPointerException(resourceManager.getText("exception.playing.field.is.null"));
         }
         final int spaltenAnzahl = newSpielfeld.getBreite();
         final int zeilenAnzahl = newSpielfeld.getHoehe();
 
         this.setAngezeigtesSpielfeldAbmessungen(spaltenAnzahl, zeilenAnzahl);
 
         /* Initialisiere Anzeigen der Sternanzahl in den Spalten */
         int anzahlSterneInSpalte;
         for (int spaltenIndex = 0; spaltenIndex < spaltenAnzahl; spaltenIndex++) {
 
             anzahlSterneInSpalte = newSpielfeld.countSterneSpalte(spaltenIndex);
             this.setAngezeigteSternAnzahlInSpalte(spaltenIndex,
                     anzahlSterneInSpalte);
         }
 
         /* Initialisiere Anzeigen der Sternanzahl in den Zeilen */
         int anzahlSterneInZeile;
         for (int zeilenIndex = 0; zeilenIndex < zeilenAnzahl; zeilenIndex++) {
 
             anzahlSterneInZeile = newSpielfeld.countSterneZeile(zeilenIndex);
             this.setAngezeigteSternAnzahlInZeile(zeilenIndex,
                     anzahlSterneInZeile);
         }
 
         /* Initialisiere Darstellungen der Spielsteine */
         Spielstein aktuellerStein;
         for (int zeilenIndex = 0; zeilenIndex < zeilenAnzahl; zeilenIndex++) {
 
             for (int spaltenIndex = 0; spaltenIndex < spaltenAnzahl; spaltenIndex++) {
 
                 aktuellerStein = newSpielfeld.getSpielstein(spaltenIndex,
                         zeilenIndex);
                 this.setAngezeigterSpielstein(spaltenIndex, zeilenIndex,
                         aktuellerStein);
             }
         }
     }
 
     /**
      * Setzt die Abmessungen der SpielfeldDarstellung. Dies hat keinen Einfluss
      * auf das der aktuellen Darstellung zugrundeliegende Spielfeld. Methode
      * verwirft alle bisher dargestellten Inhalte.
      * 
      * @param anzahlSpalten
      * @param anzahlZeilen
      */
     public void setAngezeigtesSpielfeldAbmessungen(final int anzahlSpalten,
             final int anzahlZeilen) {
 
         if (anzahlSpalten < 1 || anzahlZeilen < 1) {
             throw new IllegalArgumentException(
                     resourceManager.getText("exception.invalid.size") + " " +
                     resourceManager.getText("exception.invalid.size.column") + " " + anzahlSpalten + " " +
                     resourceManager.getText("exception.invalid.size.row") + " " + anzahlZeilen);
         }
 
         /* Nimm alles bisherige aus dem Panel raus */
         this.removeAll();
 
         /*
          * Intitialisiere Layout immer neu. Zu Spalten- und Zeilenanzahl kommt
          * je 1 hinzu, da die Anzahl der Sterne in Spalten und Zeilen noch
          * dargestellt werden muessen (oben und links). Durch
          * QuadratischeZellenGridLayout sind alle Zellen gleichgross und
          * quadratisch.
          */
         this.setLayout(new QuadratischeZellenGridLayout(anzahlZeilen + 1,
                 anzahlSpalten + 1));
 
         /* Oben links im Panel ein leeres Label als Platzhalter */
         final JLabel dummyObenLinks = createLabel();
         this.add(dummyObenLinks);
 
         /*
          * Erstelle Anzeige der Sternanzahl in den Spalten und speichere die neu
          * erstellten JLabels zusaetzlich im Array
          */
         this.sternAnzahlInSpaltenAnzeigen = new JLabel[anzahlSpalten];
         JLabel pfeilAnzahlView;
         for (int spaltenIndex = 0; spaltenIndex < anzahlSpalten; spaltenIndex++) {
 
             pfeilAnzahlView = createSternAnzahlAnzeige();
             this.sternAnzahlInSpaltenAnzeigen[spaltenIndex] = pfeilAnzahlView;
             this.add(pfeilAnzahlView);
         }
 
         /*
          * Erstelle Spielsteinansichten und Anzeige der Sternanzahl in den
          * Zeilen. Speichere beides zusaetzlich im Array.
          */
         this.spielsteinDarstellungen = new JLabel[anzahlSpalten][anzahlZeilen];
         this.sternAnzahlInZeilenAnzeigen = new JLabel[anzahlZeilen];
         JLabel spielsteinAnsicht;
         for (int zeilenIndex = 0; zeilenIndex < anzahlZeilen; zeilenIndex++) {
 
             for (int spaltenIndex = 0; spaltenIndex < anzahlSpalten; spaltenIndex++) {
 
                 /* Am Anfang jeder Zeile eine Sternanzahlanzeige einfuegen */
                 if (0 == spaltenIndex) {
                     final JLabel sternAnzahlAnzeige = createSternAnzahlAnzeige();
                     this.sternAnzahlInZeilenAnzeigen[zeilenIndex] = sternAnzahlAnzeige;
                     this.add(sternAnzahlAnzeige);
                 }
 
                 /*
                  * Erstelle neue Ansichtskomponente fuer Spielstein mit diesen
                  * Koordinaten
                  */
                 spielsteinAnsicht = createSpielsteinAnsicht(spaltenIndex,
                         zeilenIndex);
                 this.spielsteinDarstellungen[spaltenIndex][zeilenIndex] = spielsteinAnsicht;
 
                 /* Fuege Ansicht zum Panel hinzu */
                 this.add(spielsteinAnsicht);
             }
         }
         revalidate();
     }
 
     /**
      * Setzt an der angegeben Koordinate in der Darstellung das Icon neu. Dabei
      * wird das Icon des übergebenen Spielstens von der aktuellen Icon Factory
      * geladen.
      * 
      * @param spaltenIndex
      *            X
      * @param zeilenIndex
      *            Y
      * @param newSpielstein
      * @throws ArrayIndexOutOfBoundsException
      *             Wenn die angegeben Koordinaten ausserhalb der aktuellen
      *             Darstellungsausmasse liegen.
      */
     public void setAngezeigterSpielstein(final int spaltenIndex,
             final int zeilenIndex, Spielstein newSpielstein)
             throws ArrayIndexOutOfBoundsException {
 
         if ((spaltenIndex < 0)
                 || (spaltenIndex > getAngezeigteSpaltenAnzahl() - 1)
                 || (zeilenIndex < 0)
                 || (zeilenIndex > getAngezeigteZeilenAnzahl() - 1)) {
             throw new ArrayIndexOutOfBoundsException(
                     "Die uebergebenen Koordinaten Spalte:"
                             + spaltenIndex
                             + " Zeile:"
                             + zeilenIndex
                             + " liegen ausserhalb des aktuell gezeichneten Spielfelds. Aktuelle Groesse des gezeichneten Spielfelds: Spaltenanzahl: "
                             + getAngezeigteSpaltenAnzahl() + "  Zeilenanzahl: "
                             + getAngezeigteZeilenAnzahl());
         }
         if (null == newSpielstein) {
             newSpielstein = KeinStein.getInstance();
         }
         final Icon newIcon = iconFactory.getIconForSpielstein(newSpielstein);
         spielsteinDarstellungen[spaltenIndex][zeilenIndex].setIcon(newIcon);
     }
 
     /**
      * Setze die angezeigte Sternanzahl in der Spalte.
      * 
      * 
      * @param spaltenIndex
      * @param neuerWert
      * @throws ArrayIndexOutOfBoundsException
      *             Wenn die Spalte ausserhalb der aktuellen Darstellung liegt.
      */
     public void setAngezeigteSternAnzahlInSpalte(final int spaltenIndex,
             final int neuerWert) throws ArrayIndexOutOfBoundsException {
         if (spaltenIndex < 0 || spaltenIndex > getAngezeigteSpaltenAnzahl() - 1) {
             throw new ArrayIndexOutOfBoundsException("Die Spalte "
                     + spaltenIndex
                     + " liegt ausserhalb des aktuell gezeichneten Spielfelds");
         }
         this.sternAnzahlInSpaltenAnzeigen[spaltenIndex].setText(Integer
                 .toString(neuerWert));
     }
 
     /**
      * Setze die angezeigte Sternanzahl in der Zeile.
      * 
      * @param zeilenIndex
      * @param neuerWert
      * @throws ArrayIndexOutOfBoundsException
      *             Wenn die Zeile ausserhalb der aktuellen Darstellung liegt.
      */
     public void setAngezeigteSternAnzahlInZeile(final int zeilenIndex,
             final int neuerWert) throws ArrayIndexOutOfBoundsException {
 
         if (zeilenIndex < 0 || zeilenIndex > getAngezeigteZeilenAnzahl() - 1) {
             throw new ArrayIndexOutOfBoundsException("Die Zeile " + zeilenIndex
                     + " liegt ausserhalb des aktuell gezeichneten Spielfelds");
         }
         this.sternAnzahlInZeilenAnzeigen[zeilenIndex].setText(Integer
                 .toString(neuerWert));
     }
 
     /**
      * SpielfeldDarstellungsSteuerung muss gesetzt werden, wenn Mausklicks
      * irgendwelche Folgen haben sollen. Ist nicht noetig um nur eine Vorschau
      * zu erstellen.
      * 
      * @param spielfeldController
      */
     public void setSpielfeldDarstellungsSteuerung(
             final SpielfeldDarstellungsSteuerung spielfeldController) {
         this.spielfeldDarstellungsSteuerung = spielfeldController;
     }
 
     public void setIconFactory(final IconFactory newIconFactory) {
         if (null == newIconFactory) {
             throw new NullPointerException("Neue IconFactory ist null");
         }
         iconFactory = newIconFactory;
     }
 
     /**
      * Setzt das Layout neu. Akzeptiert nur QuadratischeZellenGridLayout, weil
      * sonst die Darstellung des Spielfeldes verzerrt sein kann und die Felder
      * nicht quadratisch sind.
      */
     @Override
     public void setLayout(final LayoutManager layoutManager) {
         if (null == layoutManager) {
             throw new NullPointerException("LayoutManager ist null");
         }
         if (!(layoutManager instanceof QuadratischeZellenGridLayout)) {
             throw new IllegalArgumentException(
                     "SpielfeldDarstellung funktioniert nur mit QuadratischeZellenGridLayout");
         }
         super.setLayout(layoutManager);
     }
 
     /**
      * Gibt die Anzahl der Spalten der aktuellen Spielfelddarstellung. Dies hat
      * keinen Einfluss auf das der aktuellen Darstellung zugrundeliegende
      * Spielfeld.
      * 
      * @return Die Spaltenanzahl der aktuellen Spielfelddarstellung.
      */
     public int getAngezeigteSpaltenAnzahl() {
         return spielsteinDarstellungen.length;
     }
 
     /**
      * Gibt die Anzahl der Zeilen der aktuellen Spielfelddarstellung. Dies hat
      * keinen Einfluss auf das der aktuellen Darstellung zugrundeliegende
      * Spielfeld.
      * 
      * @return Die Zeilenanzahl der aktuellen Spielfelddarstellung.
      */
     public int getAngezeigteZeilenAnzahl() {
         return spielsteinDarstellungen[0].length;
     }
 
     /**
      * Erstellt eine Spielstein-/Spielfeldelementansicht. Das Icon wird mit dem
      * Icon der aktuellen Icon Factory fuer KeinStein initialisiert. Die
      * Mausklicks bei dieser Spielsteinansicht werden an die
      * spielsteinClick()-Methode der zum Klickzeitpunkt gesetzten
      * SpielfeldDarstellungsSteuerung weitergeleitet. Wenn diese nicht gesetzt
      * ist, laufen die Klicks ins Leere (ohne Fehler).
      * 
      * @param x
      *            Spaltenindex
      * @param y
      *            Zeilenindex
      * 
      * @return Die neu erstelle SpielsteinAnsicht Komponente
      */
     private JLabel createSpielsteinAnsicht(final int x, final int y) {
         final JLabel result = createLabel();
         result.setIcon(iconFactory.getIconForSpielstein(KeinStein.getInstance()));
         result.addMouseListener(new MouseAdapter() {
             /*
              * Verhalten von mousePressed ist dynamischer als bei mouseClicked ,
              * da auch Klicks beim Bewegen der Maus genommen werden.
              */
             @Override
             public void mousePressed(final MouseEvent mouseEvent) {
                 getSpielfeldDarstellungsSteuerung().spielSteinClick(x, y,
                         mouseEvent);
             }
         });
         return result;
     }
 
     /**
      * Gibt die aktuell SpielfeldDarstellungsSteuerung zurueck, oder eine Null
      * object instanz, bei der die Mausklicks einfach ins Leere laufen, wenn die
      * SpielfeldDarstellungsSteuerung aktuell null ist.
      * 
      * @return Die aktuelle SpielfeldDarstellungsSteuerung oder eine dummy
      *         Instanz dieser Klasse.
      */
     private SpielfeldDarstellungsSteuerung getSpielfeldDarstellungsSteuerung() {
         if (null == this.spielfeldDarstellungsSteuerung) {
             return SpielfeldDarstellungsSteuerung.NULL_OBJECT_INSTANCE;
         } else {
             return this.spielfeldDarstellungsSteuerung;
         }
     }
 
     private static JLabel createLabel() {
         final JLabel result = new JLabel();
         initialisiereAllgemeinenLabelStilVon(result);
         return result;
     }
 
     private static JLabel createSternAnzahlAnzeige() {
         final JLabel result = createLabel();
        result.setFont(new Font("Serif", Font.PLAIN, 20));
         result.setForeground(Color.white);
         return result;
     }
 
     private static void initialisiereAllgemeinenLabelStilVon(final JLabel label) {
         label.setHorizontalAlignment(JLabel.CENTER);
         label.setBackground(Color.DARK_GRAY);
         label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
     }
 }
