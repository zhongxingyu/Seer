 package de.hypoport.twitterwall.twitter.mock;
 
 import java.util.List;
 
 import static de.hypoport.twitterwall.twitter.mock.MockToolbox.RANDOM;
 import static java.util.Arrays.asList;
 
 class UserMockFactory {
 
   private final static List<UserMock> users;
 
   static {
     users = asList(
         new UserMock(
             89426586L,
             "Rianne ♦",
             "Rianneeh",
             "Nederland",
             "Never say goodbye, because goodbye means going away, and going away means forgetting (Houdt van ananassen )\n▬▬▬▬▬▬▬▬▬▬▬▬♦♦♦▬▬▬▬▬▬▬▬▬▬▬▬",
             "https://pbs.twimg.com/profile_images/420647060736843776/dvPvQV71_normal.jpeg",
             "http://t.co/R0I1MqY0Uw",
             201
         ),
         new UserMock(
             1048498140L,
             "suus ♡ mainstreet ",
             "RoomSuusjex",
             "In jouwn broek .",
             ".@xDanielleOfzo_ @WiepBerendsen and @Proudreiniacx zijn mijn CutieDinnetjes. Proudsmainiac of the world. Appelsap omdat het kan xo FRIENDSAndFAMILIEBovenAlles.",
            "https://pbs.twimg.com/profile_images/286967217/n518176610_2182_bigger.jpg",
             null,
             92
         ),
         new UserMock(
             100771929L,
             "Veri",
             "Mrs__Reedus",
             "zombieland",
             "@wwwbigbaldhead ∞ ! ‹3",
             "https://pbs.twimg.com/profile_images/378800000246598616/a0a80d434df3b3d308f143db28b97df8_normal.jpeg",
             "http://t.co/MwRvPfoAA4",
             111
         ),
         new UserMock(
             118351936L,
             "35 /Mvr. Ugurdiken ♡",
             "ProudMertinator",
             "Mert & Alessandro volgen me ♥",
             "1 guy who has changed my life @Realiteittijd he mean so mutch to me he always get a smile on my face and he inspired me he is always there for me .",
             "https://pbs.twimg.com/profile_images/73212497/twitterpic_normal.JPG",
             "http://t.co/KdPTV58XdF",
             531
         ),
         new UserMock(
             605067910L,
             "Brooke Herbert",
             "brookeherberto",
             "",
             "Nebraska Cheer Center | Platinum S4\nProverbs 3:5 ∞",
             "https://pbs.twimg.com/profile_images/378800000261805304/1523027b494715c99e73b26427cd0c66_normal.png",
             null,
             469
         ),
         new UserMock(
             1633848720L,
             "Damain",
             "LordShaffer",
             "",
             "Logic will take you from A to B but imagination will take you every where. imagining my self in Estadio Santiago Bernabéu, on my whites",
             "https://pbs.twimg.com/profile_images/378800000216539925/3ca41d922a516e58a09b876f2cccc82a_normal.jpeg",
             null,
             40
         ),
         new UserMock(
             1538938237L,
             "Ander Schurller",
             "AnderSchurller",
             "Chulsee FC",
             "Tup cless LM at Chulsee FC. Yung upcumin ster. | Not affiliated with Andre Schurrle | Original Toon |",
             "https://pbs.twimg.com/profile_images/378800000534440648/80e1b0a4a5e78931e83c00bb4b6b4d09_bigger.jpeg",
             null,
             795
         ),
         new UserMock(
             762676536L,
             "•toniann•",
             "sdf",
             "lohanthony tweeted me 7-4-13",
             "•that kid on xfactor played in my basement•",
             "https://pbs.twimg.com/profile_images/2166444899/asshat_donky_bigger.png",
             "http://t.co/FlBRT51wky",
             705
         ),
         new UserMock(
             69300370L,
             "Tim",
             "AgentTeee",
             "In another castle",
             "German - 19 - Videogames - @planetjedward - @Pink - @LadyGaga I (un-)follow back :)",
             "https://pbs.twimg.com/profile_images/378800000120912773/ca37defb6d898eb668f4c1cab3e27a11_normal.png",
             null,
             949
         ),
         new UserMock(
             602654955L,
             "Quinty",
             "quiinty",
             "",
             "instaa ; @quiintyad - Ariana Grande '★ - ilovedance!'☆",
             "https://pbs.twimg.com/profile_images/1662510902/6457_justic99075_1__bigger.jpg",
             null,
             105
         ),
         new UserMock(
             1043700392L,
             "Blubidiblubblub.",
             "JustCyba",
             "Doesn't matter ~",
             "l| Floid | Frodo | Rick | Steve | Sui |l  \r\n~ Sei du selbst die Veränderung , die du dir wünschst für diese Welt ~",
             "https://pbs.twimg.com/profile_images/364466579/RuthiePicture_normal.jpg",
             null,
             82
         ),
         new UserMock(
             738733428L,
             "Killa Cam",
             "Its_Blub",
             "",
             "they call me blub",
             "https://pbs.twimg.com/profile_images/3565794736/7f390db4dddd0a26f0cf71100dabe83f_normal.jpeg",
             null,
             342
         ),
         new UserMock(
             313538359L,
             "Chloe",
             "Chloe__Eva",
             "Bath, England",
             "",
             "https://pbs.twimg.com/profile_images/378800000547276338/81e30d390b9d8af2061cc8a629e40804_bigger.jpeg",
             null,
             113
         ),
         new UserMock(
             270615239L,
             "Gail Brand",
             "gail_brand",
             "London/Kent",
             "Trombone player. Composer. Improvised Music/Jazz. On the wireless sometimes. Kvetcher. Noisy. http://t.co/XJJCZOYjEh",
             "https://pbs.twimg.com/profile_images/378800000690450329/58dbbe1cf4b40a5d33b2bc2583363280_bigger.jpeg",
             "http://t.co/W0KjRPkxYL",
             1567
         ),
         new UserMock(
             170625432L,
             "Ninjin Juice 2",
             "NinjinJuice2",
             "China",
             "I'm a ninja",
             "https://pbs.twimg.com/profile_images/378800000550212937/20870ba9ec36289566bce57f128f1bd5_normal.jpeg",
             null,
             156
         ),
         new UserMock(
             16583513L,
             "Nancy Spigos",
             "NancySpigos",
             "London, Canada",
             "A single mom of three angels *sighs happily* A romance writer *rubs stiff neck* A coffee drinker *smiles* A reader *smile widens*",
             "https://pbs.twimg.com/profile_images/2036336637/21181060716963950_wDEUmwdJ_c_normal.jpg",
             null,
             532
         )
     );
   }
 
   static UserMock createRandomUser() {
     return users.get(RANDOM.nextInt(users.size()));
   }
 
 }
