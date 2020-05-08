 package Roma.Cards;
 
 import Roma.PlayArea;
 
 import java.util.HashMap;
 
 /**
  * User: Andrew
  * Date: 16/05/12
  * Time: 6:59 PM
  */
 public class CardFactory {
     private HashMap<String, CardBase> cardList = new HashMap<String, CardBase>();
     private PlayArea playArea;
 
     public CardFactory(PlayArea playArea) {
         this.playArea = playArea;
         cardList.put(Aesculapinum.NAME, new Aesculapinum(playArea));
         cardList.put(Architectus.NAME, new Architectus(playArea));
         cardList.put(Basilica.NAME, new Basilica(playArea));
         cardList.put(Centurio.NAME, new Centurio(playArea));
         cardList.put(Consiliarius.NAME, new Consiliarius(playArea));
 
         cardList.put(Consul.NAME, new Consul(playArea));
         cardList.put(Essedum.NAME, new Essedum(playArea));
         cardList.put(Forum.NAME, new Forum(playArea));
         cardList.put(Gladiator.NAME, new Gladiator(playArea));
         cardList.put(Haruspex.NAME, new Haruspex(playArea));
 
         cardList.put(Legat.NAME, new Legat(playArea));
         cardList.put(Legionarius.NAME, new Legionarius(playArea));
         cardList.put(Machina.NAME, new Machina(playArea));
         cardList.put(Mercator.NAME, new Mercator(playArea));
         cardList.put(Mercatus.NAME, new Mercatus(playArea));
 
         cardList.put(Nero.NAME, new Nero(playArea));
         cardList.put(Onager.NAME, new Onager(playArea));
         cardList.put(Praetorianus.NAME, new Praetorianus(playArea));
         cardList.put(Scaenicus.NAME, new Scaenicus(playArea));
         cardList.put(Senator.NAME, new Senator(playArea));
 
         cardList.put(Sicarius.NAME, new Sicarius(playArea));
         cardList.put(Templum.NAME, new Templum(playArea));
         cardList.put(TribunusPlebis.NAME.replaceAll("\\sP", "p"), new TribunusPlebis(playArea));
         cardList.put(Turris.NAME, new Turris(playArea));
         cardList.put(Velites.NAME, new Velites(playArea));
 
         cardList.put(Kat.NAME, new Kat(playArea));
        cardList.put(GrimReaper.NAME, new Kat(playArea));
        cardList.put(TelephoneBox.NAME, new Kat(playArea));
     }
 
     public CardHolder getCard(String cardName) {
         CardBase cardBase = cardList.get(cardName);
         CardHolder cardHolder = null;
         if (cardBase != null) {
             cardHolder = cardBase.makeOne(playArea);
             //assert(cardHolder.getName().equalsIgnoreCase(cardName));
         }
         return cardHolder;
     }
 }
