 /*
  * Eine Instanz der Klasse Tree stellt einen baumförmigen gerichteten Graphen dar, 
  * in dem von jedem Knoten beliebig viele, mit einem Label versehene Kanten ausgehen. 
  * Die Label auf Kanten sind sortierbar; das heißt, sie unterstützen eine Methode compareTo 
  * (wie auf Seite 103 des Skriptums). Knoten des Graphen werden nach außen nicht direkt, 
  * sondern nur durch Iteratoren über die von den Knoten ausgehenden Kanten dargestellt. 
  * Je zwei Iteratoren haben unterschiedliche Identitäten, auch wenn sie über die vom selben 
  * Knoten ausgehenden Kanten iterieren.
  */
 public class Tree<A>{
 	
 	//enthält nur Wurzel ohne Kanten
 	public Tree(){
 		
 	}
 	
 	/*
 	 * retourniert einen Iterator des Typs AssocIter (mit geeigneten Typparameterersetzungen), 
 	 * der über die Label aller von der Wurzel des Baums ausgehenden Kanten in aufsteigender 
 	 * Reihenfolge (entsprechend compareTo) iteriert. Die Methode insert des Iterators fügt nur 
 	 * dann eine neue Kante mit einem neuen Label hinzu, wenn es vom selben Knoten aus noch keine 
 	 * Kante mit einem gleichen Label (entsprechend compareTo) gibt. Die über den Iterator zugänglichen 
 	 * assoziierten Objekte entsprechen Iteratoren des Typs AssocIter, welche (so wie hier für den 
 	 * Wurzelknoten beschrieben) über die Label der Kanten iterieren, die von dem Knoten ausgehen, 
 	 * der über die Kante erreichbar ist, dessen Label zuletzt von next zurückgegeben wurde. 
 	 * Jeder Aufruf von assoc (sowohl in Instanzen von Tree als auch in Iteratoren) erzeugt einen 
 	 * neuen Iterator.
 	 */
 	public Iter<AssocIter> assoc(){
 		return null;
 	}
 	
 	/*
 	 * erzeugt einen neuen Iterator des Typs Iter, der eine Tiefensuche über dem Baum macht und 
 	 * die Label der dabei besuchten Kanten sowie für jeden besuchten Blatt-Knoten (also jeden Knoten 
 	 * ohne ausgehende Kanten) null zurückgibt. Tiefensuche bedeutet, dass für jede ausgehende Kante 
 	 * der gesamte darunter hängende Teilbaum abgearbeitet wird, bevor die nächsten Kante an die Reihe 
 	 * kommt. Die von einem Knoten ausgehenden Kanten werden dabei in der Reihenfolge bearteitet, 
 	 * wie sie von durch assoc erzeugten Iteratoren zurückgegeben werden. Die Methode allLabels ist 
 	 * beim Testen hilfreich.
 	 */
 	public A allLabels(){
		return null;
 	}
 }
