 package choices;
 
 import game.Choice;
 import game.Outcome;
 import game.Person;
 import game.Requirements;
 
 public class HarryYouth {
 	private Person p;
 
 	public HarryYouth(Person p) {
 		p = p;
 	}
 
 	// Choice1
 	Requirements r1[] = { new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 			new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 			new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0) };
 
 	String[] c1Story = new String[] {
 			"You leave the object alone! \nBad call on your part you have missed out on a very valuble childhood experience. \nYou should explore more in coming ages.",
 			"You are clearly going to grow up to be very self concious and closed off.\nYour choice to not only miss out on a very important experience but to also cry leaves everyone concerned about your outgoingness.",
 			"GREAT! You have just found the object you will call BaBa for the rest of your life. \nYou are showing not only the first signs of motion but also outgoingness and bravery to grab an unknown object.\nWhen you grab the object you hear beads rattle and you vigoursly shake.\nYour parents walk in and smile.",
 			"Your attempts to do something are amusing and you start to giggle.\n Your parents walk in and smile. You have just found a great skill in life...Being a goof.",
 			"Great! You have just found the object you will call MeMe for the rest of your life. \nYou are showing the first signs of motion and your ability to start shaking the object shows intelligence and gives you a new passion...Weightlifting" };
 	Outcome[] outcomeLineC1 = new Outcome[] { new Outcome(true, 0, 0, 0, 0, -3, 0),
 			new Outcome(true, 0, -2, 0, 0, -4, 0), new Outcome(true, 0, 0, 2, 0, 3, 0),
 			new Outcome(true, 5, 0, 0, 0, 2, 0), new Outcome(true, 0, 0, 6, 0, 2, 0) };
 	Choice c1 = new Choice(
 			"When you are born you see an object in the distance what do you do? \n1. Leave the object alone \n2.Leave the object and cry \n3. Crawl over to the object and grab it \n4.Flail misserably\5.Crawl over to the object and shake it up and down.",
 			c1Story, r1, outcomeLineC1, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 					.getWealth(), p.getConfidence());
 
 	// Choice2
 	String[] c2Story = new String[] {
 			"Your confidence and charisma have impressed your peers. \nYou are immensily successful and have just found some bros for life! \nYou will surely reap the benefits of being one of the cool kids later in life...Keep it up.",
 			"Ohhhh so your one of THOSE kids...",
 			"Your dedication to academia is quite apparant.\nKeep it up so your life can go further",
 			"Your leadership has united this band of misfits! \nThese are some friends you will keep forever", "", "",
 			"", "", "", "",
 			"Your peers think your statement is ingenuine and your not bro enough. 'Leave GDI'. You have failed" };
 
 	Outcome[] outcomeLineC2 = new Outcome[] { new Outcome(true, 3, 0, 0, 2, 6, 0),
 			new Outcome(true, 2, -5, 0, 0, 0, 0), new Outcome(true, 0, 6, 0, 0, -2, 0),
 			new Outcome(true, 4, 0, 0, 0, 2, 0), null, null, null, null, null, null,
 			new Outcome(true, -1, 0, 0, 0, -2, 0) };
 
 	Requirements r2[] = { new Requirements(2, 0, 0, 0, 5), new Requirements(-1, -1, -1, -1, -1),
 			new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 	Choice c2 = new Choice(
 			"You are now away from being in the isolated haven now known as 'Home' and you are entering a new place your parents call 'School'. \nWhen you enter this weird place you see kids like yourself running around. \nConfused by everything happening what do you do? \n1.Approach some of the boys that are playing blocks with eachother and say 'What up bros' .\n2.Sit in the corner and sniff and eat glue \n3.Go pick up a book and start looking at pictures. \n4.Approach one of the kids that are playing by themselves.",
 			c2Story, r2, outcomeLineC2, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 					.getWealth(), p.getConfidence());
 
 	// Choice3
 
 	String[] c3Story = new String[] {
 			"Your dedication to academia is apparaent. \nYour teacher loves you and you feel your brain growing",
 			"You and your bros go outside hit on some ladies and P some Ls. \nYou kids are clearly the kings of the castle keep it up",
 			"You again.....",
 			"You guys play some tag and hide and go seek keep growing with your peers." };
 
 	Outcome[] outcomeLineC3 = new Outcome[] { new Outcome(true, 3, 0, 0, 0, 9, 0),
 			new Outcome(true, 2, -5, 0, 0, 0, 0), new Outcome(true, 0, 5, 0, 0, -2, 0),
 			new Outcome(true, 4, 0, 0, 0, 2, 0), null, null, null, null, null, null,
 			new Outcome(true, -3, 0, 0, 0, -2, 0) };
 
 	Requirements r3[] = { new Requirements(4, 0, 0, 0, 11), new Requirements(-1, -1, -1, -1, -1),
 			new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 	Choice c3 = new Choice(
 			"Your teacher says 'Recess' and all the kids go outside what do you do? \n1.Stay indoors and read. \n2.Go outside with your bros and kick it. \n3.Stay indoors and sniff glue. \n4.Go outside and play with random kids.",
 			c3Story, r3, outcomeLineC3, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 					.getWealth(), p.getConfidence());
 
 	// Choice4
 	String[] c4Story = new String[] {
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
 
 	// boolean alive, int charisma, int intelligence, int strength, int wealth,
 	// int confidence, int age)
 	Outcome[] outcomeLineC4 = new Outcome[] { new Outcome(true, 5, 1, 0, 0, 2, 0),
 			new Outcome(true, -2, 5, -1, 0, -2, 0), new Outcome(true, 3, -2, 0, -1, 5, 0),
 			new Outcome(true, 3, 0, 0, 2, 6, 0), null, null, null, null, null, null, null, null,
 			new Outcome(true, 0, 0, 0, 0, -5, 0), new Outcome(true, 2, 0, 0, 0, 1, 0) };
 
 	// int charisma, int intelligence, int strength, int wealth, int confidence
 	Requirements r4[] = { new Requirements(0, 0, 0, 0, 0), new Requirements(-1, -1, -1, -1, -1),
 			new Requirements(0, 0, 0, 0, 5), new Requirements(0, 0, 0, 5, 0), };
 
 	Choice c4 = new Choice(
 			"You have come of age to choose what your family life will be like. \n1.Be a happy content boy that always does what their parents say. \n2.Stay in your room and study and obey your parents whenever. \n3.Your a rebel...Enough said. \n4.You follow in the excessive and bourgeois lifestyle of your parents",
 			c4Story, r4, outcomeLineC4, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 					.getWealth(), p.getConfidence());
 
 	// Choice5
 
 	String[] c5Story = new String[] {
 			"As you approach the shorts you can hear your destiny calling...Your choice is the generic.",
 			"As you approach the shorts you can hear your destiny calling...Your one of the cool kids.",
 			"As you approach the shorts you can hear your destiny calling...You are a prep master, born to join a frat.",
 			"As you approach the shorts you can hear your destiny calling...You are an artsy hipster.",
 			"As you approach the shorts you can hear your destiny calling...You are a happy hippie." };
 	// boolean alive, int charisma, int intelligence, int strength, int wealth,
 	// int confidence, int age)
 	Outcome[] outcomeLineC5 = new Outcome[] { new Outcome(true, 3, 2, 0, 0, 2, 1), new Outcome(true, 3, 0, 0, 0, 5, 0),
 			new Outcome(true, 3, 0, 0, 1, 5, 0), new Outcome(true, 4, 3, 0, 0, 0, 0),
 			new Outcome(true, 6, 0, 0, 0, 0, 0) };
 
 	Requirements r5[] = { new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0),
 			new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), new Requirements(0, 0, 0, 0, 0), };
 
 	Choice c5 = new Choice(
			"Now here is the BIG decision...\nYour parents have finally have decided to let you choose your pants...\n(Be Careful and wise this pretty much determines the game).\n1.Choose cargo shorts.\n2.Choose jeans.\n3.Choose Nantucket Reds\4.Cordaroy Overalls.\n5.Tie Die Shorts.",
 			c5Story, r5, outcomeLineC5, p, p.getAge(), p.getCharisma(), p.getIntelligence(), p.getStrength(), p
 					.getWealth(), p.getConfidence());
 
 }
