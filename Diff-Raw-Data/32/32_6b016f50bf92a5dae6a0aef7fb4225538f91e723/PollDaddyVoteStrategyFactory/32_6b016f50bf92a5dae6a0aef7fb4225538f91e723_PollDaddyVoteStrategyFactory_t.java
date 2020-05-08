 /**
  * Copyright (c) 2012, Sini
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy 
  * of this software and associated documentation files (the "Software"), to deal 
  * in the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
  * THE SOFTWARE.
  */
 
 package net.sini.nkvoter.core;
 
 import java.util.HashMap;
 
 /**
  * Created by Sini
  */
 public final class PollDaddyVoteStrategyFactory extends VoteStrategyFactory {
     private HashMap<String, String[]> candidates;
     
     /**
      * Constructs a new {@link PollDaddyVoteStrategy};
      */
     public PollDaddyVoteStrategyFactory()
     {
         candidates = new HashMap<String, String[]>();
         initCandidates();
     }
     
     @Override
     public VoteStrategy createStrategy()
     {
         return null;
     }
     public VoteStrategy createStrategy(String candID) {
         String[] relevantStrings = candidates.get(candID);
         return new PollDaddyVoteStrategy(relevantStrings[0], relevantStrings[1], relevantStrings[2]);
     }
     
     private void initCandidates()
     {
         String[] KJU_strings = new String[3];
         KJU_strings[0] = "Our Glorious Leader";
         KJU_strings[1] = "/n/113df4577acffec0e03c79cfc7210eb6/6685610?";
         KJU_strings[2] = "/vote-js.php?p=6685610&b=1&a=30279773&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129192%2C00.html&n=";
         candidates.put("KJU", KJU_strings);
         
         String[] Jon_strings = new String[3];
         Jon_strings[0] = "Jon";
         Jon_strings[1] = "/n/6b1aa0c281ebbeec4419dbcdb76a7da0/6685709?";
         Jon_strings[2] = "/vote-js.php?p=6685709&b=1&a=30280231&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129211%2C00.html&n=";
         candidates.put("Jon", Jon_strings);
         
         String[] Undoc_strings = new String[3];
         Undoc_strings[0] = "Undocumented";
         Undoc_strings[1] = "/n/e1386f1d203929ebe8c0202ab9b529bf/6685607?";
         Undoc_strings[2] = "/vote-js.php?p=6685607&b=1&a=30279757,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129191%2C00.html&n=";
         candidates.put("Undoc", Undoc_strings);
         
         String[] Stephen_strings = new String[3];
         Stephen_strings[0] = "Stephen";
         Stephen_strings[1] = "/n/45a54d304efd9783196e1db13da69194/6685714?";
         Stephen_strings[2] = "/vote-js.php?p=6685714&b=1&a=30280243,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129212%2C00.html&n=";
         candidates.put("Stephen", Stephen_strings);
         
         String[] Gabrielle_strings = new String[3];
         Gabrielle_strings[0] = "Gabrielle";
         Gabrielle_strings[1] = "/n/77beeeb2bb776cc255eb4e3fac1cb624/6685689?";
         Gabrielle_strings[2] = "/vote-js.php?p=6685689&b=1&a=30280171,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129203%2C00.html&n=";
         candidates.put("Gabrielle", Gabrielle_strings);
         
         String[] Aung_strings = new String[3];
         Gabrielle_strings[0] = "Aung";
         Gabrielle_strings[1] = "/n/f9ff5c88db083cd08a595210a3b21335/6685635?";
        Gabrielle_strings[2] = "/vote-js.php?p=6685635&b=1&a=30279879,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129196%2C00.html&n=";
 
        candidates.put("Aung", Aung_strings);
         
         String[] Christie_strings = new String[3];
         Gabrielle_strings[0] = "Christie";
         Gabrielle_strings[1] = "/n/a1494915370b2c2fa356b26a34428e7b/6685577?";
        Gabrielle_strings[2] = "/vote-js.php?p=6685577&b=1&a=30279643,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129182%2C00.html&n=";
         candidates.put("Christie", Christie_strings);
         
         String[] Hillary_strings = new String[3];
         Gabrielle_strings[0] = "Hillary";
         Gabrielle_strings[1] = "/n/1893654f84b7a11625b092f4779375c2/6685580?";
        Gabrielle_strings[2] = "/vote-js.php?p=6685580&b=1&a=30279653,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129184%2C00.html&n=";
         candidates.put("Hillary", Hillary_strings);
         
         String[] AiWeiwei_strings = new String[3];
         Gabrielle_strings[0] = "AiWeiwei";
         Gabrielle_strings[1] = "/n/274f014fe83e2dec59c9894b0ba6afa2/6685620?";
        Gabrielle_strings[2] = "/vote-js.php?p=6685620&b=1&a=30279821,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129193%2C00.html&n=";
         candidates.put("AiWeiwei", AiWeiwei_strings);
         
         String[] Morsi_strings = new String[3];
         Gabrielle_strings[0] = "Morsi";
         Gabrielle_strings[1] = "/n/f5feb0194a8f56d8ffa40f1ddd6cb0af/6685628?";
        Gabrielle_strings[2] = "/vote-js.php?p=6685628&b=1&a=30279854,&o=&va=16&cookie=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129194%2C00.html&n=";
         candidates.put("Morsi", Morsi_strings);
         
         String[] Assad_strings = new String[3];
         Gabrielle_strings[0] = "Assad";
         Gabrielle_strings[1] = "/n/cd8babebc20d724de7f9326b90871c31/6685629?";
         Gabrielle_strings[2] = "/vote-js.php?p=6685629&b=1&a=30279856,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129195%2C00.html&n=";
         candidates.put("Assad", Assad_strings);
         
         String[] ELJames_strings = new String[3];
         Gabrielle_strings[0] = "ELJames";
         Gabrielle_strings[1] = "/n/f25d6b945b051886812e322f2990bacd/6685699?";
         Gabrielle_strings[2] = "/vote-js.php?p=6685699&b=1&a=30280200,&o=&va=16&cookie=0&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129207%2C00.html&n=";
         candidates.put("ELJames", ELJames_strings);
         
         String[] Goodell_strings = new String[3];
         Gabrielle_strings[0] = "Goodell";
         Gabrielle_strings[1] = "/n/c5b26c7c51ad0c1fe4d82c9c273fb31d/6685666?";
         Gabrielle_strings[2] = "/vote-js.php?p=6685666&b=1&a=30280073,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129201%2C00.html&n=";
         candidates.put("Goodell", Goodell_strings);
         
         String[] Adelson_strings = new String[3];
         Gabrielle_strings[0] = "Adelson";
         Gabrielle_strings[1] = "/n/7a0bb8ade981eb9c2bc955e0587e7180/6685590?";
         Gabrielle_strings[2] = "/vote-js.php?p=6685590&b=1&a=30279691,&o=&va=16&c=1&url=http%3A//www.time.com/time/specials/packages/article/0%2C28804%2C2128881_2128882_2129186%2C00.html&n=";
         candidates.put("Adelson", Adelson_strings);
     }
 }
