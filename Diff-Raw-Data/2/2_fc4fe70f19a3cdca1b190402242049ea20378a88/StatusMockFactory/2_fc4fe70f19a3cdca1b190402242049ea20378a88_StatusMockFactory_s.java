 package de.hypoport.twitterwall.twitter.mock;
 
 import java.util.List;
 
 import static de.hypoport.twitterwall.twitter.mock.MockToolbox.RANDOM;
 import static java.util.Arrays.asList;
 
 class StatusMockFactory {
 
   private final static List<StatusMock> status;
 
   static {
     status = asList(
         new StatusMock(
            "blub invasie",
             "web"
         ),
         new StatusMock(
             "blub blub blub , te lui om m'n mobiel op te laden ,",
             "web"
         ),
         new StatusMock(
             "Blub. Ich hab Bonnie Strange's, oder wie auch immer, Ehemann angeschrieben. xD ich find ihn ja heiß",
             "<a href=\"http://www.tweetcaster.com\" rel=\"nofollow\">TweetCaster for Android</a>"
         ),
         new StatusMock(
             "blub ik ben paars aahah #webcamtoy http://t.co/274ec5qWO0",
             "<a href=\"http://webcamtoy.com/\" rel=\"nofollow\">Webcam Toy</a>"
         ),
         new StatusMock(
             "Blub blub blub https://t.co/qdllc5LB8t",
             "<a href=\"http://vine.co\" rel=\"nofollow\">Vine - Make a Scene</a>"
         ),
         new StatusMock(
             "RT @AnderSchurller: Actully am blub Schurller.",
             "<a href=\"http://blackberry.com/twitter\" rel=\"nofollow\">Twitter for BlackBerry®</a>"
         ),
         new StatusMock(
             "Actully am blub Schurller.",
             "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>"
         ),
         new StatusMock(
             "blub blub blub\uD83D\uDC19\uD83D\uDC19\uD83D\uDC19",
             "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>"
         ),
         new StatusMock(
             "@Blub_1 @cpvoros Because I didn't know you will be mentioned when I reply to the retweet",
             "web"
         ),
         new StatusMock(
             "Vvissen zeggen blub",
             "<a href=\"http://twitter.com/download/android\" rel=\"nofollow\">Twitter for Android</a>"
         ),
         new StatusMock(
             "◎ニュース速報+＠２ｃｈ◎ 【社会】 「名前を亞堕夢（アダム）にして」「優万旗（やんた）にしろ」 ”キラキラネーム”を推してくる友人や親戚に苦悩する人々 http://t.co/AW2siGokW4",
             "web"
         ),
         new StatusMock(
             "Rinna und Kyra meinen beide, in der Hölle sei es warm. Okey. #CooleSache #Blub #MeinBruderMeintIchMachZuWenigHashtags",
             "web"
         ),
         new StatusMock(
             "@mz_duprey14 same here I was baussin it",
             "<a href=\"http://twitter.com/download/android\" rel=\"nofollow\">Twitter for Android</a>"
         ),
         new StatusMock(
             "Really living it up on a Friday night. We're catching up on Long Lost Family and have a good blub.",
             "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>"
         ),
         new StatusMock(
             "@petemaskreplica elephants always make me blub. Always. #elephantisland",
             "<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>"
 
         ),
         new StatusMock(
             "#blub #summer013 #waterfun https://t.co/w9Mxfotp34",
             "<a href=\"http://vine.co\" rel=\"nofollow\">Vine - Make a Scene</a>"
         )
     );
   }
 
   static StatusMock createRandomStatus() {
     return status.get(RANDOM.nextInt(status.size()));
   }
 
 }
