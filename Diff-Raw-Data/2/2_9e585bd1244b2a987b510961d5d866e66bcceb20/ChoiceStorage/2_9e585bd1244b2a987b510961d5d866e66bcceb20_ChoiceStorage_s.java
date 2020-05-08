 package game;
 
 import java.util.ArrayList;
 
 public class ChoiceStorage {
 	private int currentChoice = -1;
 	private ArrayList<Choice> choices = new ArrayList<Choice>();
 
 	/**
 	 * Builds a new instance of ChoiceStorages
 	 */
 	public ChoiceStorage(Person p) {
 
 		// Choice1
 		final Requirements hr1[] = { new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0) };
 
 		final String[] hc1Story = new String[] {
 				"You leave the object alone! \nBad call on your part you have missed out on a very valuble childhood experience. \nYou should explore more in coming ages.",
 				"You are clearly going to grow up to be very self concious and closed off.\nYour choice to not only miss out on a very important experience but to also cry leaves everyone concerned about your outgoingness.",
 				"GREAT! You have just found the object you will call BaBa for the rest of your life. \nYou are showing not only the first signs of motion but also outgoingness and bravery to grab an unknown object.\nWhen you grab the object you hear beads rattle and you vigoursly shake.\nYour parents walk in and smile.",
 				"Your attempts to do something are amusing and you start to giggle.\n Your parents walk in and smile. You have just found a great skill in life...Being a goof.",
 				"Great! You have just found the object you will call MeMe for the rest of your life. \nYou are showing the first signs of motion and your ability to start shaking the object shows intelligence and gives you a new passion...Weightlifting" };
 		final Outcome[] houtcomeLineC1 = new Outcome[] { new Outcome(true, 0, 0, 0, 0, -3, 0),
 				new Outcome(true, 0, -2, 0, 0, -4, 0), new Outcome(true, 0, 0, 2, 0, 3, 0),
 				new Outcome(true, 5, 0, 0, 0, 2, 0), new Outcome(true, 0, 0, 6, 0, 2, 0) };
 		final Choice hc1 = new Choice(
 				"When you are born you see an object in the distance what do you do? \n1. Leave the object alone \n2.Leave the object and cry \n3. Crawl over to the object and grab it \n4.Flail misserably\5.Crawl over to the object and shake it up and down.",
 				hc1Story, hr1, houtcomeLineC1, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 						.getWealth(), p.getConfidence());
 
 		// Choice2
 		final String[] hc2Story = new String[] {
 				"Your confidence and charisma have impressed your peers. \nYou are immensily successful and have just found some bros for life! \nYou will surely reap the benefits of being one of the cool kids later in life...Keep it up.",
 				"Ohhhh so your one of THOSE kids...",
 				"Your dedication to academia is quite apparant.\nKeep it up so your life can go further",
 				"Your leadership has united this band of misfits! \nThese are some friends you will keep forever", "",
 				"", "", "", "", "",
 				"Your peers think your statement is ingenuine and your not bro enough. 'Leave GDI'. You have failed" };
 
 		final Outcome[] houtcomeLineC2 = new Outcome[] { new Outcome(true, 3, 0, 0, 2, 6, 0),
 				new Outcome(true, 2, -5, 0, 0, 0, 0), new Outcome(true, 0, 6, 0, 0, -2, 0),
 				new Outcome(true, 4, 0, 0, 0, 2, 0), null, null, null, null, null, null,
 				new Outcome(true, -1, 0, 0, 0, -2, 0) };
 
 		final Requirements hr2[] = { new Requirements(2, 0, 0, 0, 5), new Requirements(-1, -1, -1, -1, -1),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 		final Choice hc2 = new Choice(
 				"You are now away from being in the isolated haven now known as 'Home' and you are entering a new place your parents call 'School'. \nWhen you enter this weird place you see kids like yourself running around. \nConfused by everything happening what do you do? \n1.Approach some of the boys that are playing blocks with eachother and say 'What up bros' .\n2.Sit in the corner and sniff and eat glue \n3.Go pick up a book and start looking at pictures. \n4.Approach one of the kids that are playing by themselves.",
 				hc2Story, hr2, houtcomeLineC2, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 						.getWealth(), p.getConfidence());
 
 		// Choice3
 
 		final String[] hc3Story = new String[] {
 				"Your dedication to academia is apparaent. \nYour teacher loves you and you feel your brain growing",
 				"You and your bros go outside hit on some ladies and P some Ls. \nYou kids are clearly the kings of the castle keep it up",
 				"You again.....", 
 				"You guys play some tag and hide and go seek keep growing with your peers." };
 
 		final Outcome[] outcomeLineC3 = new Outcome[] { new Outcome(true, 3, 0, 0, 0, 9, 0),
 				new Outcome(true, 2, -5, 0, 0, 0, 0), new Outcome(true, 0, 5, 0, 0, -2, 0),
 				new Outcome(true, 4, 0, 0, 0, 2, 0), null, null, null, null, null, null,
 				new Outcome(true, -3, 0, 0, 0, -2, 0) };
 
 		final Requirements hr3[] = { new Requirements(4, 0, 0, 0, 11), new Requirements(-1, -1, -1, -1, -1),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 		final Choice hc3 = new Choice(
 				"Your teacher says 'Recess' and all the kids go outside what do you do? \n1.Stay indoors and read. \n2.Go outside with your bros and kick it. \n3.Stay indoors and sniff glue. \n4.Go outside and play with random kids.",
 				hc3Story, hr3, outcomeLineC3, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 						.getWealth(), p.getConfidence());
 
 		// Choice4
 		final String[] hc4Story = new String[] {
 				"Your pretty much the Cleaver family. \nYou have family dinners and movie nights on the weekend, your parents grant you a ton of responsility and push you to new heights",
 				"Wow your personality is really showing now... needless to say your life is pretty boring",
 				"Your parents hate you but the kids respect you. You snek out on the reg with the rest of your boys teepeing and smoking tea. You and your boys are the talk of the town, boys want to be you and girls want their nap mat next to yours. lets just hope your parents wont retaliate that much.",
 				"Good choice bro. Your birthdays are filled with Vinard Vines boxes and Brookes Brothers bags. Your country club lifestyle has made you the talk of the school. THe boys want to be you and the girls want their nap mat next to you. May your croakies hang with pride...",
 				"",
 				"",
 				"",
 				"",
 				"",
 				"",
 				"",
 				"",
 				"HAHA you think YOU could be a rebel?? Your parents laugh and joke about your 'phases' and the kids around school think your more of a tool than you already are.",
 				"Although your intentions shape what you will strive for in your life...your family's wealth is no where near that level." };
 
 		// boolean alive, int charisma, int intelligence, int strength, int
 		// wealth,
 		// int confidence, int age)
 		final Outcome[] houtcomeLineC4 = new Outcome[] { new Outcome(true, 5, 1, 0, 0, 2, 0),
 				new Outcome(true, -2, 5, -1, 0, -2, 0), new Outcome(true, 3, -2, 0, -1, 5, 0),
 				new Outcome(true, 3, 0, 0, 2, 6, 0), null, null, null, null, null, null, null, null,
 				new Outcome(true, 0, 0, 0, 0, -5, 0), new Outcome(true, 2, 0, 0, 0, 1, 0) };
 
 		// int charisma, int intelligence, int strength, int wealth, int
 		// confidence
 		final Requirements hr4[] = { new Requirements(0, 0, 0, 0, 0), new Requirements(-1, -1, -1, -1, -1),
 				new Requirements(0, 0, 0, 0, 5), new Requirements(0, 0, 0, 5, 0), };
 
 		final Choice hc4 = new Choice(
 				"You have come of age to choose what your family life will be like. \n1.Be a happy content boy that always does what their parents say. \n2.Stay in your room and study and obey your parents whenever. \n3.Your a rebel...Enough said. \n4.You follow in the excessive and bourgeois lifestyle of your parents",
 				hc4Story, hr4, houtcomeLineC4, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 						.getWealth(), p.getConfidence());
 
 		// Choice5
 
 		final String[] hc5Story = new String[] {
 				"As you approach the shorts you can hear your destiny calling...Your choice is the generic.",
 				"As you approach the shorts you can hear your destiny calling...Your one of the cool kids.",
 				"As you approach the shorts you can hear your destiny calling...You are a prep master, born to join a frat.",
 				"As you approach the shorts you can hear your destiny calling...You are an artsy hipster.",
 				"As you approach the shorts you can hear your destiny calling...You are a happy hippie." };
 		// boolean alive, int charisma, int intelligence, int strength, int
 		// wealth,
 		// int confidence, int age)
 		final Outcome[] houtcomeLineC5 = new Outcome[] { new Outcome(true, 3, 2, 0, 0, 2, 1),
 				new Outcome(true, 3, 0, 0, 0, 5, 0), new Outcome(true, 3, 0, 0, 1, 5, 0),
 				new Outcome(true, 4, 3, 0, 0, 0, 0), new Outcome(true, 6, 0, 0, 0, 0, 0) };
 
 		final Requirements hr5[] = { new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 		final Choice hc5 = new Choice(
				"Now here is the BIG decision...\nYour parents have finally have decided to let you choose your pants...\n(Be Careful and wise this pretty much determines the game).\n1.Choose cargo shorts.\n2.Choose jeans.\n3.Choose Nantucket Reds\4.Cordaroy Overalls.\n5.Tie Die Shorts.",
 				hc5Story, hr5, houtcomeLineC5, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 						.getWealth(), p.getConfidence());
 
 		// ///////// Constructs the Prompts ///////////
 
 		final String pt1 = "Welcome to your first day of highschool! Today you get to decide how you want to act.\n1: Become a sports jock.\n2: Hang with the popular crowd.\n3: Study for your classes.\n4: Do nothing";
 		final String pt2 = "You get invited to a party. Everyone seems to be having fun. Drugs and alcohol are present.\n1: Get drunk and go crazy\n2: Drive your drunk friend home\n3: Socialize\n4: Stare at your phone";
 		final String pt3 = "It is the day before finals. The year is almost over and summer is within your reach. Do you: \n1: Spend your time studying for your tests.\n2: Hang out with your friends and cram the morning of the test.\n3: Don't study and try to cheat off of someone else.\n4: Just kinda chill.";
 		final String pt4 = "It is senior year and about time you made up your mind about College. Let's apply! \n1: You apply to schools with Division A Lax teams. \n2: You apply to law school. \n3: You apply to the best engineering schools in the country. \n4: You decide maybe college isn't your thing.";
 		final String pt5 = "It's time to find a full-time job. \n1: Get a job. \n2: Go back to live at your parents' house.";
 
 		// ///////// Constructs the outcome string arrays ///////////
 
 		final String[] sl1 = {
 				"You get recruited by the Lax team because of your sick Lax skills. Lax.",
 				"You meet some new friends. Everyone seems to want to sit next to you at lunch.",
 				"You hit the books. Your grades stay steady and the teachers seem to enjoy your participation in class.",
 				"You just kinda coast along. Nobody seems to know a whole lot about you.", null, "You win", null, null,
 				null, null, "The bros stare at you blankly. You really thought you could lax with them?",
 				"It's obvious to them that you aren't that cool.",
 				"You lose concentration while trying to study. Maybe you have ADHD?", "Error 808: you suck." };
 
 		final String[] sl2 = {
 				"You wake up on a park bench in the next town over wearing a traffic cone as hat.",
 				"The next day, your friend thanks you profusely for your wise actions of the previous night.",
 				"You meet a girl named Maria and hit it off.",
 				"You stay in the corner and keep to yourself. No one seems to notice your presence.",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"You take one drink and throw up. You're not exactly the life of the party,",
 				"Your friend says he doesn't need any help. He gets in his car and crashes it into a tree almost immediately.",
 				"You can't talk over the music. You don't end up meeting anyone", "Error 808: you suck." };
 
 		final String[] sl3 = {
 				"After hours of studying you feel ready for your tests. You do well on all of them.",
 				"You don't do awesome on your tests, but you feel as though your friends appreciate your carefree style.",
 				"You do well on your tests: maybe a little too well. Nobody has proof of anything, but you lose the trust of those around you.",
 				"You do pretty average, although your not even sure your teachers know your name.", null, null, null,
 				null, null, null, "You fall asleep while studying. You don't do great on your tests",
 				"You fail your tests, and your friends think you should spend more time on your academics",
 				"You get caught! You get a zero on all of your tests", "Error 808: you suck." };
 
 		final String[] sl4 = { "You make it in to Lax U. Congratulations!",
 				"You make it in to Lawyer U. Congratulations!", "You make it in to Engineering U. Congratulations!",
 				"You take a job at McDoodles to make an income. Congratulations?", null, null, null, null, null, null,
 				"Your lax skills aren't quite up to snuff. You get rejected.",
 				"You aren't a great public speaker. You get rejected.",
 				"You aren't that great at math and science. You get rejected.", "Error 808: you suck." };
 
 		final String[] sl5 = { "Welcome to adulthood.", "Your parents aren't very happy about this.", null, null, null,
 				null, null, null, null, null, "You aren't qualified enough.", "Error 808: you suck." };
 
 		// ///////// Constructs outcomes and outcome arrays ///////////
 
 		final Outcome o1 = new Outcome(true, 2, -1, 5, 0, 5, 0);
 		final Outcome o2 = new Outcome(true, 5, -1, 0, 2, 5, 0);
 		final Outcome o3 = new Outcome(true, -1, 5, 0, 5, 2, 0);
 		final Outcome o4 = new Outcome(true, 2, 2, 2, 2, 2, 0);
 		final Outcome owin = new Outcome(true, 100, 100, 100, 100, 100, 0);
 
 		final Outcome o1a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o2a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o3a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o4a = new Outcome(true, 0, 0, 0, 0, 0, 0);
 
 		final Outcome[] ol1 = { o1, o2, o3, o4, null, owin, null, null, null, null, o1a, o2a, o3a, o4a };
 
 		final Outcome o5 = new Outcome(true, 7, -4, 4, -2, 7, 0);
 		final Outcome o6 = new Outcome(true, 3, 5, 0, 0, 4, 0);
 		final Outcome o7 = new Outcome(true, 6, 0, 0, -1, 7, 0);
 		final Outcome o8 = new Outcome(true, 2, 2, 2, 2, 2, 0);
 
 		final Outcome o5a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o6a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o7a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o8a = new Outcome(true, 0, 0, 0, 0, 0, 0);
 
 		final Outcome[] ol2 = { o5, o6, o7, o8, null, null, null, null, null, null, o5a, o6a, o7a, o8a };
 
 		final Outcome o9 = new Outcome(true, 0, 6, 0, 3, 3, 0);
 		final Outcome o10 = new Outcome(true, 6, 0, 3, 0, 3, 0);
 		final Outcome o11 = new Outcome(true, -2, 8, 0, -2, 8, 0);
 		final Outcome o12 = new Outcome(true, 2, 2, 2, 2, 2, 0);
 
 		final Outcome o9a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o10a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o11a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o12a = new Outcome(true, 0, 0, 0, 0, 0, 0);
 
 		final Outcome[] ol3 = { o9, o10, o11, o12, null, null, null, null, null, null, o9a, o10a, o11a, o12a };
 
 		final Outcome o13 = new Outcome(true, 2, -1, 5, 0, 5, 0);
 		final Outcome o14 = new Outcome(true, 5, -1, 0, 2, 5, 0);
 		final Outcome o15 = new Outcome(true, -1, 5, 0, 5, 2, 0);
 		final Outcome o16 = new Outcome(true, 2, 2, 2, 2, 2, 0);
 
 		final Outcome o13a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o14a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o15a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o16a = new Outcome(true, 0, 0, 0, 0, 0, 0);
 
 		final Outcome[] ol4 = { o13, o14, o15, o16, null, null, null, null, null, null, o13a, o14a, o15a, o16a };
 
 		final Outcome o17 = new Outcome(true, 2, 2, 2, 2, 2, 5);
 		final Outcome o18 = new Outcome(true, 0, 0, 0, 0, 0, 0);
 
 		final Outcome o17a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 		final Outcome o18a = new Outcome(true, -1, -1, -1, -1, -1, 0);
 
 		final Outcome[] ol5 = { o17, o18, null, null, null, null, null, null, null, null, o17a, o18a };
 
 		// ///////// Constructs requirements and requirement arrays ///////////
 
 		final Requirements r1 = new Requirements(2, 0, 2, 0, 2);
 		final Requirements r2 = new Requirements(2, 0, 0, 2, 2);
 		final Requirements r3 = new Requirements(1, 3, 0, 1, 1);
 		final Requirements r4 = new Requirements(0, 0, 0, 0, 0);
 		final Requirements rwin = new Requirements(0, 0, 0, 0, 0);
 
 		final Requirements[] ra1 = { r1, r2, r3, r4, null, rwin };
 
 		final Requirements r5 = new Requirements(6, 0, 0, 0, 6);
 		final Requirements r6 = new Requirements(2, 6, 2, 0, 2);
 		final Requirements r7 = new Requirements(6, 0, 0, 0, 6);
 		final Requirements r8 = new Requirements(0, 0, 0, 0, 0);
 
 		final Requirements[] ra2 = { r5, r6, r7, r8 };
 
 		final Requirements r9 = new Requirements(0, 3, 0, 0, 0);
 		final Requirements r10 = new Requirements(1, 0, 0, 1, 1);
 		final Requirements r11 = new Requirements(0, 3, 0, 0, 3);
 		final Requirements r12 = new Requirements(0, 0, 0, 0, 0);
 
 		final Requirements[] ra3 = { r9, r10, r11, r12 };
 
 		final Requirements r13 = new Requirements(0, 0, 10, 5, 5);
 		final Requirements r14 = new Requirements(10, 0, 0, 5, 5);
 		final Requirements r15 = new Requirements(0, 10, 0, 5, 5);
 		final Requirements r16 = new Requirements(0, 0, 0, 0, 0);
 
 		final Requirements[] ra4 = { r13, r14, r15, r16 };
 
 		final Requirements r17 = new Requirements(2, 2, 2, 2, 2);
 		final Requirements r18 = new Requirements(0, 0, 0, 0, 0);
 
 		final Requirements[] ra5 = { r17, r18, };
 
 		// ///////// Constructs the choices ///////////
 
 		Choice c1 = new Choice(pt1, sl1, ra1, ol1, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c2 = new Choice(pt2, sl2, ra2, ol2, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c3 = new Choice(pt3, sl3, ra3, ol3, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c4 = new Choice(pt4, sl4, ra4, ol4, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c5 = new Choice(pt5, sl5, ra5, ol5, p, p.getAge(), 0, 0, 0, 0, 0);
 
 		final String[] A1Story = {
 				"You become a McDoodles worker",
 				"You become a mechanic",
 				"You become a sports star",
 				"You become an engineer",
 				"You become a CEO",
 				"You become a politician",
 				"DO NOT READ! in three days you will be kissed by the love of your life, but if you do not repost this to 5 other videos, you will be murdered in two days by a pack of wild puffins!",
 				null, null, null, "You fail to become a McDoodles worker. You may wish to reevaluate your life.",
 				"You fail to become a mechanic. Perhaps a lower-skill job would be better for you.",
 				"You fail to become a sports star. Try to get stronger.",
 				"You fail to become an engineer. You should work at McDoodles instead.", "You fail to become a CEO.",
 				"You fail to become a politician. You're not good enough at wooing voters." };
 
 		final Outcome[] A1Outcomes = { new Outcome(true, 0, 0, 0, 5, 1, 0), new Outcome(true, 0, 1, 1, 10, 3, 0),
 				new Outcome(true, 5, 0, 10, 20, 10, 0), new Outcome(true, 0, 15, 0, 15, 10, 0),
 				new Outcome(true, 5, 10, 0, 25, 7, 0), new Outcome(true, 10, 7, 0, 15, 10, 0),
 				new Outcome(true, 100, 100, 100, -100, 100, 100), null, null, null, null,
 				new Outcome(true, 0, 0, 0, 0, -20, 0), new Outcome(true, 0, 0, 0, 0, -10, 0),
 				new Outcome(true, 0, 0, 0, 0, -10, 0), new Outcome(true, 0, 0, 0, 0, -10, 0),
 				new Outcome(true, 0, 0, 0, 0, -10, 0), new Outcome(true, 0, 0, 0, 0, -10, 0)
 
 		};
 
 		Requirements[] A1reqs = { new Requirements(1, 1, 1, 1, 1), new Requirements(3, 20, 15, 5, 5),
 				new Requirements(20, 5, 30, 20, 10), new Requirements(10, 30, 5, 15, 10),
 				new Requirements(30, 20, 5, 10, 10), new Requirements(20, 30, 5, 20, 15),
 				new Requirements(0, 0, 0, 0, 0) };
 
 		Choice choiceA1 = new Choice(
 				"You decide to get a job. The choices are \n1: 'McDoodles worker'\n2: 'Mechanic'\n3: 'Sports Star'\n4: 'Engineer'\n5: 'CEO'\n6: or 'Politician'",
 				A1Story, A1reqs, A1Outcomes, p, 2, 0, 0, 0, 0, 0);
 
 		final String A2printText = "You are bored at home. You decide to do something. You can\n1: read a book\n2: work out, or \n3:go to a party";
 
 		final String[] A2Story = { "You decide to read a book. You are now more intelligent",
 				"You decide to work out. You are now stronger", "You decide to go to a party. Charisma goes up", null,
 				null, null, null, null, null, null, "You managed to fail at reading. Are you even literate?",
 				"You failed to work out. frynotsureifweakorstupid.jpg",
 				"You failed to go to a party. I've given up hope." };
 
 		final Requirements[] A2Reqs = { new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0) };
 
 		final Outcome[] A2Outcomes = { new Outcome(true, 0, 10, 0, 0, 0, 0), new Outcome(true, 0, 0, 10, 0, 0, 0),
 				new Outcome(true, 10, 0, 0, 0, 0, 0), null, null, null, null, null, null, null,
 				new Outcome(true, -5, -5, -5, -5, -5, 0), new Outcome(true, -5, -5, -5, -5, -5, 0),
 				new Outcome(true, -5, -5, -5, -5, -5, 0) };
 
 		Choice choiceA2 = new Choice(A2printText, A2Story, A2Reqs, A2Outcomes, p, 2, 0, 0, 0, 0, 0);
 
 		final String A3printText = "You are very lonely. Would you like to try to get married? (1 yes, 2 no)";
 
 		final String[] A3Story = {
 				"Congratulations! You managed to convince someone to spend their entire life with you! You are now married.",
 				"You decided that married life is not for you.",
 				"If this text is displayed, something is horribly wrong",
 				"Seriously, if you can see this, they're coming", "I'm not kidding. You'd better run",
 				"Seriously, RUN!", "Well clearly you're not listening to me",
 				"I'm going to sit tight while they eat you", "and I'm not going to feel any regret",
 				"Well, I guess this is a lost cause, bye",
 				"You failed to convince somebody to marry you. You must be ugly, poor, or both",
 				"How did you possibly fail at not getting married?! I'm ashamed of you."
 
 		};
 		final Requirements[] A3reqs = { new Requirements(30, 20, 15, 20, 25), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 		final Outcome[] A3Outcomes = { new Outcome(true, 10, 0, 0, -10, 10, 0), new Outcome(true, 0, 0, 0, 0, 0, 0),
 				null, null, null, null, null, null, null, null, new Outcome(true, -5, 0, 0, 0, -20, 0),
 				new Outcome(true, -10, -10, -10, -10, -40, 0), };
 		Choice choiceA3 = new Choice(A3printText, A3Story, A3reqs, A3Outcomes, p, 3, 0, 0, 0, 0, 0);
 
 		final String A4printText = "You really hate people. You're so angry you consider becoming a serial killer. Would you like to become a serial killer?";
 
 		final String[] A4Story = {
 				"You succeed at becoming a serial killer, you monster",
 				"I suppose you don't have the killer instinct",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"At the house of your first hit, you are discovered, and the police are called. You are shot by the police and die.",
 				"I'm not sure how, but you managed to fail at not becoming a serial killer. I'm going to kill you because of your sheer incompetence. Self-control isn't even an attribute in this game!" };
 
 		final Outcome[] A4Outcomes = { new Outcome(true, 5, 10, 10, -5, 10, 0), new Outcome(true, 0, 0, 0, 0, 0, 0),
 				null, null, null, null, null, null, null, null, new Outcome(false, -100, -100, -100, -100, -100, 0),
 				new Outcome(false, -1000, -1000, -1000, -1000, -1000, 0)
 
 		};
 
 		final Requirements[] A4reqs = { new Requirements(40, 30, 20, 20, 0), new Requirements(0, 0, 0, 0, 0) };
 
 		Choice choiceA4 = new Choice(A4printText, A4Story, A4reqs, A4Outcomes, p, 3, 30, 0, 0, 0, 0);
 
 		final String A5PrintText = "Kids? (yes/maybe/no)";
 
 		final String[] A5Story = {
 				"You've had a baby! prepare for the next 18 years well",
 				"Maybe? Well, I suppose I'll choose for you and give you a baby, you seem qualified enough. Your reluctance will not, however, go unpunished",
 				"I suppose having a kid isn't for everybody", null, null, null, null, null, null, null,
 				"You didn't manage to have a baby. You aren't very good at this, are you?",
 				"Maybe? You seem horribly unqualified to have a kid, so I'll spare the baby and not let you have one",
 				"You failed (somehow) at not having a kid, so you'll get one anyway" };
 
 		final Requirements[] A5Reqs = { new Requirements(5, 0, 5, 0, 0), new Requirements(5, 15, 10, 20, 0),
 				new Requirements(0, 0, 0, 0, 0) };
 
 		final Outcome[] A5Outcomes = { new Outcome(true, 5, 5, 5, -20, 5, 1), new Outcome(true, 5, 5, 5, -30, -10, 1),
 				new Outcome(true, 0, 0, 0, 0, 0, 1), null, null, null, null, null, null, null,
 				new Outcome(true, 0, 0, 0, -5, -15, 1), new Outcome(true, 0, 0, 0, 0, 0, 1),
 				new Outcome(true, -5, -5, -5, -40, -5, 1) };
 
 		Choice choiceA5 = new Choice(A5PrintText, A5Story, A5Reqs, A5Outcomes, p, 3, 0, 0, 0, 0, 0);
 
 		final String wpt1 = "After what has been an eternity of work and focus, it's time to retire.  As you know, you're getting older.  What will you focus on after retirement?\n1. Invest in stocks\n2. Expand your wisdom\n3. Go after younger women\n4. Relax and do what you want";
 		final String wpt2 = "You sit at your house and you realize there is yard work to be done outside, but due to your age, you're not sure if it would be best to do it yourself.  What do you do?\n1. Hire gardener\n2. Take advantage of 'slave labor' by paying the neighborhood kid 1 cent per weed he picks\n3. Go out and try to do it yourself\n4. Put off the yard work ";
 		final String wpt3 = "Time to update your will.  Who do you leave your most prized possesions and fortunes to?\n1. Your family\n2. Your mistress\n3. Charity\n4. Dedicate your net worth to maintaining your legacy.";
 		final String wpt4 = "Your family wants you to move into a nursing home. How do you respond?\n1. Move into the nursing home\n2. Move to a tropical island paradise \n3. Refuse to move into the nursing home and stay in your house\n4. Force your family to let you stay with them";
 		final String wpt5 = "Your doctor tells you that you are on the verge of death.  What is the last thing you decide to do before you die?\n1. Go on the vacation of a lifetime\n2. Climb Mount Everest\n3. Solidify your legacy\n4. Relax and wait to die in peace with the Wu Wei wisdom you have gained through a lifetime of experience";
 
 		final String[] wsl1 = { "You succesfully invest in stocks. You make bank.",
 				"You successfully expand your wisdom. You are now a wise old man.",
 				"You successfully go after younger women. Even in your later days, you can sure still pull.",
 				"You decide to relax and do nothing. Wu Wei.", null, null, null, null, null, null,
 				"You fail investing in stocks, and lose a substantial amount of your money.",
 				"You fail to expand your wisdom. Seems like your intelligence will not make great gains.",
 				"You fail to get any younger women. Maybe your pickup lines threw them off.",
 				"You cannot relax. Maybe you should try meditation." };
 
 		final Outcome[] wo1 = { new Outcome(true, 2, 2, 0, 20, 10, 0), new Outcome(true, 2, 20, 0, 2, 10, 0),
 				new Outcome(true, 20, 0, 5, 5, 20, 0), new Outcome(true, 20, 0, 20, 0, 20, 0), null, null, null, null,
 				null, null, new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 				new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 
 		};
 
 		final Requirements[] wr1 = { new Requirements(0, 15, 0, 15, 15), new Requirements(0, 15, 0, 5, 15),
 				new Requirements(15, 5, 15, 15, 20), new Requirements(0, 0, 15, 0, 20), };
 
 		Choice wc1 = new Choice(wpt1, wsl1, wr1, wo1, p, p.getAge(), 0, 0, 0, 0, 0);
 
 		final String[] wsl2 = {
 				"You hire a gardener, and he takes care of your yard problem. Smart move.",
 				"You exploitative old man. Way to take advantage of the youth. Nonetheless, the yard work is done.",
 				"You go outside and get the yard work done. Your health is impressive at your age.",
 				"You put off the yard work. The yard work remains unfinished, but at least you don't have to spend money.",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"You can't hire a good gardenenr, and thus your yard remains unkempt",
 				"You can't seem to hire a young man to do your yard work. Maybe they are afraid of you.",
 				"Why did you try that? You know your health isn't what it used to be.  The yard work is not ever finished.",
 				"You put off the yard work. The yard work remains unfinished, but at least you don't have to spend money." };
 
 		final Outcome[] wo2 = { new Outcome(true, 0, 5, 10, -1, 10, 0), new Outcome(true, 10, 15, 10, 5, 15, 0),
 				new Outcome(true, 5, 5, 0, 10, 20, 0), new Outcome(true, 0, 0, 10, 10, 0, 0), null, null, null, null,
 				null, null, new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 				new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 
 		};
 
 		final Requirements[] wr2 = { new Requirements(5, 0, 0, 15, 10), new Requirements(10, 10, 0, 10, 20),
 				new Requirements(0, 0, 35, 0, 25), new Requirements(0, 0, 0, 0, 15), };
 
 		Choice wc2 = new Choice(wpt2, wsl2, wr2, wo2, p, p.getAge(), 0, 0, 0, 0, 0);
 
 		final String[] wsl3 = {
 				"You leave your most important possesions to your family. You are quite the family man.",
 				"You leave your estate to your mistress. If your wife outlives you, you can only imagine what she would say.",
 				"You leave your fortune to charity. You are quite the philanthropist.",
 				"You set up a fund to create giant statues and public services in your name.  You shall be remembered forever.",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"Your ego prevents you from leaving your possesions to your family. You should feel ashamed.",
 				"You fail to leave your estate to your mistress, and are caught by your wife. You get divorced.",
 				"You wouldn't really do that? Let's be honest. Better keep all that money away from those money grabbing idiots.",
 				"Come on? We both know you wouldn't do that. That's not in your capacity." };
 
 		final Outcome[] wo3 = { new Outcome(true, 10, 0, 0, 10, 0, 0), new Outcome(true, 5, 0, 0, 0, 15, 0),
 				new Outcome(true, 15, 0, 0, 15, 0, 0), new Outcome(true, 15, 0, 0, 5, 20, 0), null, null, null, null,
 				null, null, new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 				new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 
 		};
 
 		final Requirements[] wr3 = { new Requirements(10, 15, 0, 0, 10), new Requirements(25, 0, 0, 0, 25),
 				new Requirements(15, 0, 0, 5, 10), new Requirements(30, 0, 0, 30, 30), };
 
 		Choice wc3 = new Choice(wpt3, wsl3, wr3, wo3, p, p.getAge(), 0, 0, 0, 0, 0);
 
 		final String[] wsl4 = {
 				"You move into the nursing home without complaint. You agree with your family that it is best.",
 				"You offer a better idea and buy a remote island tropical paradise. Sounds nice.",
 				"You successfully stay in your home by putting your family off.",
 				"You come up with an idea that makes your family wish they never even suggested you move in to the nursing home. You move into your younger family's home tomorrow.",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"You can't move into the nursing home. Even though you want it, you know it just won't work.",
 				"Really? Since when could you afford a tropical island home?",
 				"You fail to stay in your home, and your family is insistent. You are forced to go to the nursing home.",
 				"Your family does not let that happen, and you are not insistent enough. You go to the nursing home." };
 
 		final Outcome[] wo4 = { new Outcome(true, 10, 0, 0, 10, 5, 0), new Outcome(true, 10, 0, 5, 0, 20, 0),
 				new Outcome(true, 10, 0, 0, 10, 10, 0), new Outcome(true, 10, 0, 0, 15, 10, 0), null, null, null, null,
 				null, null, new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 				new Outcome(true, -1, -1, -1, -1, -1, 0), new Outcome(true, -1, -1, -1, -1, -1, 0),
 
 		};
 
 		final Requirements[] wr4 = { new Requirements(0, 0, 0, 0, 0), new Requirements(10, 0, 20, 40, 25),
 				new Requirements(10, 10, 15, 15, 15), new Requirements(15, 15, 10, 0, 20), };
 
 		Choice wc4 = new Choice(wpt4, wsl4, wr4, wo4, p, p.getAge(), 0, 0, 0, 0, 0);
 
 		final String[] wsl5 = {
 				"You go on the vacation of your dreams. You die in peace, sleeping on the beach.",
 				"You begin to climb Mount Everest, and as you reach your arms into the sky at the top of the mountain, you die in complete ecstacy.",
 				"You succesfully solidify your legacy. Your dead body is coated in gold and made into a statue at the center of your home town.",
 				"You die without desire in your mind, and with wisdom in your mind. Peace eternalizes you, and you are reincarnated.",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"You can't afford the tropical island vacation, and you die in a hospital from a heart attack.",
 				"Your health is definitely not up for that, and you die a painful and slow death of an unusual disease you catch traveling to the mountain.",
 				"You fail to solidify your legacy, and die as an unknown man with little money and few friends.",
 				"You have not embraced Wu Wei, and you die a life of eternal suffering and dispair." };
 		final Outcome[] wo5 = { new Outcome(false, 20, 0, 15, 15, 30, 0), new Outcome(false, 20, 0, 25, 0, 25, 0),
 				new Outcome(false, 45, 0, 0, 0, 45, 0), new Outcome(false, 30, 70, 0, 0, 25, 0), null, null, null,
 				null, null, null, new Outcome(false, -1, -1, -1, -1, -1, 0), new Outcome(false, -1, -1, -1, -1, -1, 0),
 				new Outcome(false, -1, -1, -1, -1, -1, 0), new Outcome(false, -1, -1, -1, -1, -1, 0),
 
 		};
 
 		final Requirements[] wr5 = { new Requirements(20, 0, 30, 30, 30), new Requirements(15, 0, 40, 20, 30),
 				new Requirements(40, 0, 0, 30, 35), new Requirements(15, 40, 0, 0, 0), };
 
 		Choice wc5 = new Choice(wpt5, wsl5, wr5, wo5, p, p.getAge(), 0, 0, 0, 0, 0);
 
 		choices.add(hc1);
 		choices.add(hc2);
 		choices.add(hc3);
 		choices.add(hc4);
 		choices.add(hc5);
 		choices.add(c1);
 		choices.add(c2);
 		choices.add(c3);
 		choices.add(c4);
 		choices.add(c5);
 		choices.add(choiceA1);
 		choices.add(choiceA2);
 		choices.add(choiceA3);
 		choices.add(choiceA4);
 		choices.add(choiceA5);
 		choices.add(wc1);
 		choices.add(wc2);
 		choices.add(wc3);
 		choices.add(wc4);
 		choices.add(wc5);
 
 	}
 
 	/**
 	 * returns a boolean that is true if the person's attributes satisfy all of
 	 * the requirements for the choice c
 	 * 
 	 * @param p
 	 * @param c
 	 * @return whether or not the person p is qualified for choice c
 	 */
 	private boolean isQualified(Person p, Choice c) {
 
 		if (!(p.getCharisma() >= c.getCharismaReq())) {
 			return false;
 		} else if (!(p.getConfidence() >= c.getConfindenceReq())) {
 			return false;
 		} else if (!(p.getIntelligence() >= c.getIntelligenceReq())) {
 			return false;
 		} else if (!(p.getStrength() >= c.getStrengthReq())) {
 			return false;
 		} else if (!(p.getWealth() >= c.getWealthReq())) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param p
 	 *            A person object
 	 * 
 	 * @return A choice that the person is qualified for
 	 */
 	/**
 	 * @param p
 	 * @return
 	 */
 	public Choice getNextChoice(Person p) {
 		Choice selectedChoice = choices.get(++currentChoice);
 		boolean qualified = false;
 
 		while (!qualified) {
 			if (this.isQualified(p, selectedChoice)) {
 				qualified = true;
 			} else {
 				selectedChoice = choices.get(currentChoice + 1);
 			}
 		}
 		return selectedChoice;
 	}
 
 }
