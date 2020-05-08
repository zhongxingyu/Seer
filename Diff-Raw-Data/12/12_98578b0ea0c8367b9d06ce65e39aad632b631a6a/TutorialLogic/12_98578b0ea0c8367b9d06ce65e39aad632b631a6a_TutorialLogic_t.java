 package controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import view.Icon;
 import view.TDI;
 import view.TDI.TDIState;
 import view.TutorialView;
 
 class TutorialLogic implements Runnable {
     private final TutorialView tv = new TutorialView();
     private final ArrayList<TDI> tdis;
 
     public TutorialLogic(ArrayList<TDI> tdis) {
         this.tdis = tdis;
     }
 
     @Override
     public void run() {
         try {
             //First Step
             Icon temp = tdis.get(0).getIcons().get((tdis.get(0).getIcons().size() - 1));
             //First step
             tv.setText("1/11 - Desktop Modus\nBitte wählen Sie den Icon " + temp.getName() + " an der Position " + temp.getPosition().x + ","
                     + temp.getPosition().y + " durch Drehen des zugeordneten TDIs, bis das Symbol markiert ist. ");
             while (!tdis.get(0).getIcons().get(0).equals(temp)) ; //Wait period
             //Second Step
            tv.setText("2/11 - Desktop Modus\nLocken Sie das Icon durch Kippen des TDIs auf die rechte Seite");
             while (!tdis.get(0).getIcons().get(0).equals(temp) && !tdis.get(0).getLocked()) ; //Wait period
             //Third
             tv.setText("3/11 - Desktop Modus, gesperrt\nBewegen Sie den Icon an eine andere Position");
             while (tdis.get(0).getIcons().get(0).getPosition() == temp.getPosition()) ;
             //Fourth
             tv.setText("4/11 Desktop Modus, gesperrt\nSehr gut, Sie haben den TDI erfolgreich bewegt. TDIs können viele verschiedene Modie annehmen. Wie Sie vielleicht im Fortschrittbalken bemerkt haben, lehren wir Sie gerade die Befehle im Desktop-Modus."
                     + " Wenn Sie den TDI rechts kippen wird es gesperrt. Wenn der TDI gesperrt ist, sind Sie in der Lage, Symbole zu anderen Positionen zu bewegen und wenn ein Programm geöffnet ist, können sie dieses schließen, minimieren und maximieren. Entsperren Sie den TDI durch Kippen nach rechts.");
             while (tdis.get(0).getLocked()) ;
             int i = ProgramHandler.getRunningPrograms().size();
             tv.setText("5/11 Desktop Modus\nGut gemacht! Die grüne LED leuchtet auf, wenn der TDI gesperrt ist und dreht sich wieder ab, wenn der TDI nicht verschlossen ist. Bewegen Sie nun den TDI nach unten/oben auf Ihrer Arbeitsplattform, um ein Programm zu starten");
             while (i == ProgramHandler.getRunningPrograms().size()) ;
             tv.setText("6/11 In-App Modus\nAls Sie das Program gestartet haben, haben Sie vielleicht bemerkt, dass ein TDI unten/oben auf dem Tisch blieb, der TDI ist jetzt in Taskleiste Modus. Der TDI, der sich in die Mitte "
                     + "des Arbeitsplatzes bewegt hat, befindet sich in In-App-Modus. In-App-Modus ist eine Sondermodus, wo Entwickler sogenannte \"plugins\" entwickeln können, um TDI Funktionalität in verschiedenen Programmen unterstützen zu können. Beachten Sie, dass dieses Programm nur Fenstern "
                     + "manipuliert und nicht in der ist Programme selbst zu manipulieren. Um dies zu können, "
                     + "benötigen Sie die so genannten Plug-Ins. Um In-App-Modus verlassen zu können, neigen Sie den TDI nach rechts");//TODO check correctness
             for (i = 0; i < tdis.size() && !tdis.get(i).getState().equals(TDIState.inapp); i++) ;
             while (tdis.get(i).getState().equals(TDIState.inapp)) ;
             tv.setText("7/11 Fenster Modus\nDer TDI ist nun im Fenster Modus. Im Fenster Modus können Sie die Fenster selbst manipulieren: verschieben, minimieren: nach unten neigen, maximieren: nach oben neigen, schließen: nach unten neigen. Lassen Sie uns versuchen, ein paar Funktionalitäten zu testen. Bewegen Sie den Fenster durch Bewegen des TDIs");
             TDI tdi = tdis.get(i);
             while (Arrays.equals(tdi.getPosition(), tdis.get(i).getPosition())) ;
             tv.setText("8/11 Fenster Modus\nSchöner Move! Neigen Sie den TDI nach unten, um das Programm zu minimieren");
             if (ProgramHandler.isDesktopMode())
                 tv.setText("9/11 Desktop Modus\nDer TDI ist nun wieder im Desktop-Modus. Wenn alle geöffneten Programme minimiert werden, dann geht der TDI zurück in den Desktop-Modus. Jetzt ist es wieder für die Icons verantwortlich. "
                         + "Neigen Sie den TDI, der im Taskleiste Modus ist (Rote LED an) nach oben, um alle Programme, die minimiert sind, wiederherzustellen");
             else
                 tv.setText("9/11 In-App Modus\nDer TDI ist nun wieder im In-App-Modus. Wenn Sie ein Programm minimieren und das nächste geöffnete Programm im Hintergrund wird automatisch fokussiert, geht ihr TDI zurück in In-App-Modus. "
                         + "Neigen Sie den TDI, der im Taskleiste Modus ist (Rote LED an) nach oben, um alle Programme, die minimiert sind, wiederherzustellen");
             while (ProgramHandler.getNonMinimized() > 0) ;
             tv.setText("10/11 In-App Modus\nSehr gut! Um zwischen indivdualle Programmen wechseln zu können, müssen sie den Taskleiste-TDI nach rechts oder links drehen, um diese zu minimieren, neigen Sie nach unten. "
                     + "Lassen Sie uns versuchen, die Größe des aktuellen fokussierten Fensters zu ändern, welches nicht so kompliziert ist. Um das zu tun, bewegen Sie den Taskleiste TDI dicht an den In-App-Modus TDI und warten Sie ein Sekunde. Die TDIs sind in Skala Modus.");
             while (!tdi.isScale()) ;
             tv.setText("11/11 Skala Modus\nUm die Größe des Programms ändern zu können, müssen sie den TDI bewegen. Spielen Sie sich mit dem Skala Modus");
             Thread.sleep(3000);
             tv.setText("Gut gemacht! Sie haben das Tutorial beendet. Jetzt wissen Sie alles, was Sie benötigen, um die TDIs nutzen zu können. Plugins können heruntergeladen werden, sofern vorhanden, um weitere Programme in In-App-Modus manipulieren zu können. Lesen Sie sich die Bedienungsanleitung an für weitere Informationen");
         } catch (Exception e) {
             tv.setText("Ein Fehler ist aufgetreten, das Tutorial wurde beendet");
         }
     }
 }
