 package com.example.columbiaprivacyapp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class TreeMenuFragment extends SherlockFragment{
 	protected View view; 
 	protected LinkedHashMap<Item,ArrayList<Item>> groupList;
 	protected ExpandableListView expandableListView;
 
 	private LinkedHashMap<Item, ArrayList<Item>> myHistory;
 
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		MainActivity.getInstance().invalidateOptionsMenu();
 		view = inflater.inflate(R.layout.treemenu, container, false);
 
 		//Creating AutoCompleteTextView, adding adapter, and notifying to update view
 		AutoCompleteTextView autoView = (AutoCompleteTextView) view.findViewById(R.id.edit_message);
 		String[] itemOptions = getResources().getStringArray(R.array.edit_message);
 
 		ArrayAdapter<String> theAdapter = 
 				new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, itemOptions);
 		autoView.setAdapter(theAdapter);
 		((BaseAdapter) autoView.getAdapter()).notifyDataSetChanged();
 
 
 		//Creating Button and Setting Listener
 		Button b = (Button) view.findViewById(R.id.post_blacklist_button);
 		b.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				AutoCompleteTextView editText=(AutoCompleteTextView) view.findViewById(R.id.edit_message);
 				String blackListItem = editText.getText().toString();
 				editText.setText("");
 				MainActivity.getInstance().postBlackListItem(blackListItem);
 				//refreshes the rest 
 				MainActivity.getInstance().invalidateOptionsMenu();
 			}
 		});
 
 		//Creating ExpandableListView Menu Below..
 		initViews(view);
 
 		if(view != null) { return view; }
 
 		((ViewGroup) autoView.getParent()).removeView(autoView);
 		container.addView(autoView);
 

 		return view;
 	}
 
 
 
 	private void initViews(View theView){
 		if(groupList==null) {
 			initContactList();
 		}

 		expandableListView = (ExpandableListView) theView.findViewById(R.id.expandableListView);
 		ExpandableAdapter adapter = new ExpandableAdapter(getActivity(), expandableListView, groupList);
 		expandableListView.setAdapter(adapter);
 
 	}
 	private void initContactList(){
 		groupList = new LinkedHashMap<Item,ArrayList<Item>>();
 
 		ArrayList<Item> groupsList = fetchGroups();
 		Log.i("GroupsListSize",String.valueOf(groupsList.size()));
 
 		for(Item item:groupsList){
 			String[] ids = item.id.split(",");
 			ArrayList<Item> groupMembers =new ArrayList<Item>();
 			for(int i=0;i<ids.length;i++){
 				String groupId = ids[i];
 				groupMembers.addAll(fetchGroupMembers(groupId));
 			}
 
 			item.name = item.name +" ("+groupMembers.size()+")";
 			groupList.put(item,groupMembers);
 		}
 
 	}
 
 
 
 	private ArrayList<Item> fetchGroups(){
 		ArrayList<Item> groupList = new ArrayList<Item>();
 		//List each group 
 		for(int i=0; i<21; i++){
 			Item item = new Item();
 			String groupName; 
 			if(i==0) {
 				item.id = groupName = "Active Life";
 			}
 			else if(i==1) {
 				item.id = groupName = "Arts & Entertainment";
 			}
 			else if(i==2) {
 				item.id = groupName = "Automotive";
 			}
 			else if(i==3) {
 				item.id = groupName = "Beauty & Spas";
 			}
 			else if(i==4) {
 				item.id =groupName = "Bicycles";
 			}
 			else if(i==5) {
 				item.id =groupName = "Education";
 			} 
 			else if(i==6) {
 				item.id = groupName = "Event Planning & Services";
 			}
 			else if(i==7) {
 				item.id=groupName = "Food";
 			}
 			else if(i==8) {
 				item.id=groupName = "Health & Medical";
 			}
 			else if(i==9) {
 				item.id=groupName = "Home Services";
 			}
 			else if(i==10) {
 				item.id=groupName = "Hotels & Travel";
 			}
 			else if(i==11) {
 				item.id=groupName = "Local Flavor";
 			}
 			else if(i==12) {
 				item.id= groupName = "Local Services";
 			}
 			else if(i==13) {
 				item.id=groupName = "Mass Media";
 			}
 			else if(i==14) {
 				item.id=groupName = "Night Life";
 			}
 			else if(i==15) {
 				item.id=groupName = "Pets";
 			}
 			else if(i==16) {
 				item.id=groupName = "Professional Services";
 			}
 			else if(i==17) {
 				item.id=groupName = "Public Services & Government";
 			}
 			else if(i==18) {
 				item.id=groupName = "Real Estate";
 			}
 			else if(i==19) {
 				item.id=groupName = "Religious Organizations";
 			}
 			else if(i==20) {
 				item.id=groupName = "Restaurants";
 			}
 			else {
 				item.id=groupName = "Shopping";
 			}
 			item.name = groupName;
 			groupList.add(item);	
 		}
 		Collections.sort(groupList,new Comparator<Item>() {
 
 			public int compare(Item item1, Item item2) {
 
 				return item2.name.compareTo(item1.name)<0
 						?0:-1;
 			}
 		});
 		return groupList;
 	}
 
 	private ArrayList<Item> fetchGroupMembers(String groupId){
 		ArrayList<Item> groupMembers = new ArrayList<Item>(); 
 
 		if(groupId.equals("Active Life")) {
 			String[] toAdd = {"active life","amateur sports teams","amusement parks","aquariums","archery","badminton","bathing area","beaches","bicycle paths","bike rentals","boating","bowling","bungee jumping","climbing","disc golf","diving","free diving","scuba diving","experiences","fishing","fitness & instruction","barre classes","boot camps","boxing","dance studios","gyms","martial arts","pilates","swimming lessons/schools","tai chi","trainers","yoga","gliding","go karts","golf","gun/rifle ranges","gymnastics","hang gliding","hiking","horse racing","horseback riding","hot air balloons","indoor playcentre","kids activities","kiteboarding","lakes","laser tag","lawn bowling","leisure centers","mini golf","mountain biking","nudist","paddleboarding","paintball","parks","dog parks","skate parks","playgrounds","public plazas","rafting/kayaking","recreation centers","rock climbing","sailing","skating rinks","skiing","skydiving","soccer","spin classes","sport equipment hire","sports clubs","squash","summer camps","surfing","swimming pools","tennis","trampoline parks","tubing","zoos","zorbing"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Arts & Entertainment")) {
 			String[] toAdd = {"arts & entertainment","arcades","art galleries","betting centers","botanical gardens","casinos","castles","choirs","cinema","cultural center","festivals","christmas markets","fun fair","general festivals","trade fairs","jazz & blues","marching bands","museums","music venues","opera & ballet","performing arts","professional sports teams","psychics & astrologers","race tracks","social clubs","stadiums & arenas","street art","tablao flamenco","ticket sales","wineries"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Automotive")) {
 			String[] toAdd ={"automotive","auto detailing","auto glass services","auto loan providers","auto parts & supplies","auto repair","boat dealers","body shops","car dealers","car stereo installation","car wash","gas & service stations","motorcycle dealers","motorcycle repair","oil change stations","parking","rv dealers","smog check stations","tires","towing","truck rental","windshield installation & repair"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Beauty & Spas")) {
 			String[] toAdd = {"beauty & spas","barbers","cosmetics & beauty supply","day spas","eyelash service","hair extensions","hair removal","laser hair removal","hair salons","blow dry/out services","hair extensions","hair stylists","men's hair salons","makeup artists","massage","medical spas","nail salons","perfume","permanent makeup","piercing","rolfing","skin care","tanning","tattoo"} ;
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Bicycles")) {
 			String[] toAdd = {"bicycles","bike associations","bike repair","bike shop","special bikes"};
 			groupMembers = addMyMembers(toAdd);
 		}
 
 		else if(groupId.equals("Education")) {
 			String[] toAdd = {"education","adult education","college counseling","colleges & universities","educational services","elementary schools","middle schools & high schools","preschools","private schools","private tutors","religious schools","special education","specialty schools","art schools","cpr classes","circus schools","cooking schools","cosmetology schools","dance schools","driving schools","first aid classes","flight instruction","language schools","massage schools","swimming lessons/schools","vocational & technical school","test preparation","tutoring centers"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Event Planning & Services ")) {
 			String[] toAdd = {"event planning & services","bartenders","boat charters","cards & stationery","caterers","clowns","djs","hotels","magicians","musicians","officiants","party & event planning","party bus rentals","party equipment rentals","party supplies","personal chefs","photographers","event photography","session photography","venues & event spaces","videographers","wedding planning"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Financial Services")) {
 			String[] toAdd = {"financial services","banks & credit unions","check cashing/pay-day loans","financial advising","insurance","investing","tax services"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Food")) {
 			String[] toAdd = {"food","bagels","bakeries","beer, wine & spirits","beverage store","breweries","bubble tea","butcher","csa","churros","coffee & tea","convenience stores","delicatessen","desserts","do-it-yourself food","donairs","donuts","ethic grocery","farmers market","food delivery services","food trucks","friterie","gelato","grocery","ice cream & frozen yogurt","internet cafes","juice bars & smoothies","kiosk","mulled wine","organic stores","parent cafes","patisserie/cake shop","pretzels","shaved ice","specialty food","candy stores","cheese shops","chocolatiers & shops","ethnic food","fruits & veggies","health markets","herbs & spices","meat shops","seafood markets","street vendors","tea rooms","wineries","zapiekanka"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Health & Medical")) {
 			String[] toAdd = {"health & medical","acupuncture","cannabis clinics","chiropractors","counseling & mental health","dental hygienists","mobile clinics","storefront clinics","dentists","cosmetic dentists","endodontists","general dentistry","oral surgeons","orthodontists","pediatric dentists","periodontists","diagnostic services","diagnostic imaging","laboratory testing","doctors","allergists","anesthesiologists","audiologist","cardiologists","cosmetic surgeons","dermatologists","ear nose & throat","family practice","fertility","gastroenterologist","gerontologists","internal medicine","naturopathic/holistic","neurologist","obstetricians & gynecologists","oncologist","ophthalmologists","orthopedists","osteopathic physicians","pediatricians","podiatrists","proctologists","psychiatrists","pulmonologist","sports medicine","surgeons","tattoo removal","urologists","hearing aid providers","hearing aids","home health care","hospice","hospitals","lactation services","laser eye surgery/lasik","massage therapy","medical centers","bulk billing","osteopaths","walk-in clinics","medical spas","medical transportation","midwives","nutritionists","occupational therapy","optometrists","pharmacy","physical therapy","reflexology","rehabilitation center","retirement homes","saunas","speech therapists","traditional chinese medicine","urgent care","weight loss centers"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Home Services")) {
 			String[] toAdd = {"home services","building supplies","carpenters","carpet installation","carpeting","contractors","electricians","flooring","garage door services","gardeners","handyman","heating & air conditioning/hvac","home cleaning","home inspectors","home organization","home theatre installation","home window tinting","interior design","internet service providers","irrigation","keys & locksmiths","landscape architects","landscaping","lighting fixtures & equipment","masonry/concrete","movers","painters","plumbing","pool cleaners"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Hotel & Travel")) {
 			String[] toAdd = {"hotels & travel","airports","bed & breakfast","campgrounds","car rental","guest houses","hostels","hotels","motorcycle rental","rv parks","rv rental","resorts","ski resorts","tours","train stations","transportation","airlines","airport shuttles","dolmus station","ferries","limos","public transportation","taxis","water taxis","travel services","vacation rental agents","vacation rentals"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Local Flavor")) {
 			String[] toAdd = {"local flavor"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Local Services")) {
 			String[] toAdd = {"local services","appliances & repair","bail bondsmen","bike repair/maintenance","carpet cleaning","child care & day care","community service/non-profit","couriers & delivery services","dry cleaning & laundry","electronics repair","funeral services & cemeteries","furniture reupholstery","it services & computer repair","data recovery","mobile phone repair","jewelry repair","junk removal & hauling","notaries","pest control","printing services","record labels","recording & rehearsal studios","recycling center","screen printing","screen printing/t-shirt printing","self storage","sewing & alterations","shipping centers","shoe repair","snow removal","watch repair","youth club"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Mass Media")) {
 			String[] toAdd = {"mass media","print media","radio stations","television stations"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Nightlife")) {
 			String[] toAdd = {"nightlife","adult entertainment","bars","beach bars","beer bar","champagne bars","cocktail bars","dive bars","gay bars","hookah bars","hotel bar","irish pub","lounges","pubs","sports bars","wine bars","beer gardens","coffeeshops","comedy clubs","country dance halls","dance clubs","dance restaurants","fasil music","jazz & blues","karaoke","music venues","piano bars","pool halls"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Pets")) {
 			String[] toAdd = {"pets","animal shelters","horse boarding","pet services","dog walkers","pet boarding/pet sitting","pet groomers","pet training","pet stores","veterinarians"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Professional Services")) {
 			String[] toAdd = {"professional services","accountants","advertising","architects","boat repair","career counseling","editorial services","employment agencies","graphic design","internet service providers","lawyers","bankruptcy law","business law","criminal defense law","dui law","divorce & family law","employment law","estate planning law","general litigation","immigration law","personal injury law","real estate law","life coach","marketing","matchmakers","office cleaning","personal assistants","private investigation","public relations","security services","talent agencies","taxidermy","translation services","video/film production","web design"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Public Services & Government")) {
 			String[] toAdd = {"public services & government","authorized postal representative","community centers","courthouses","departments of motor vehicles","embassy","fire departments","landmarks & historical buildings","libraries","police departments","post offices","registry office","tax office"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Real Estate")) {
 			String[] toAdd = {"real estate","apartments","commercial real estate","home staging","mortgage brokers","property management","real estate agents","real estate services","shared office spaces","university housing","roofing","security systems","shades & blinds","solar installation","television service providers","tree services","utilities","window washing","windows installation","real estate","apartments","commercial real estate","home staging","mortgage brokers","property management","real estate agents","real estate services","shared office spaces","university housing"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Religious Organizations")) {
 			String[] toAdd = {"religious organizations","buddhist temples","churches","hindu temples","mosques","synagogues"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else if(groupId.equals("Restaurants")) {
 			String[] toAdd = {"restaurants","afghan","african","senegalese","south african","american","american","arabian","argentine","armenian","asian fusion","asturian","australian","austrian","baguettes","bangladeshi","barbeque","basque","bavarian","beer garden","beer hall","belgian","bistros","black sea","brasseries","brazilian","breakfast & brunch","british","buffets","bulgarian","burgers","burmese","cafes","cafeteria","cajun/creole","cambodian","canadian","canteen","caribbean","dominican","haitian","puerto rican","trinidadian","catalan","chech","cheesesteaks","chicken shop","chicken wings","chinese","cantonese","dim sum","fuzhou","hakka","henghwa","hokkien","shanghainese","szechuan","teochew","comfort food","corsican","creperies","cuban","curry sausage","cypriot","czech/slovakian","danish","delis","diners","eastern european","ethiopian","fast food","filipino","fish & chips","fondue","food court","food stands","french","french southwest","galician","gastropubs","georgian","german","baden","eastern german","hessian","northern german","palatine","rhinelandian","giblets","gluten-free","greek","halal","hawaiian","himalayan/nepalese","hot dogs","hot pot","hungarian","iberian","indian","indonesian","international","irish","island pub","israeli","italian","altoatesine","apulian","calabrian","cucina campana","emilian","friulan","ligurian","lumbard","roman","sardinian","sicilian","tuscan","venetian","japanese","izakaya","ramen","teppanyaki","jewish","kebab","korean","kosher","kurdish","laos","laotian","latin american","colombian","salvadoran","venezuelan","live/raw food","lyonnais","malaysian","mamak","nyonya","meatballs","mediterranean","mexican","middle eastern","egyptian","lebanese","milk bars","modern australian","modern european","mongolian","moroccan","new zealand","night food","open sandwiches","oriental","pakistani","parent cafes","parma","persian/iranian","peruvian","pita","pizza","polish","pierogis","portuguese","potatoes","poutineries","pub food","rice","romanian","rotisserie chicken","rumanian","russian","salad","sandwiches","scandinavian","scottish","seafood","serbo croatian","signature cuisine","singaporean","soul food","soup","southern","spanish","arroceria / paella","steakhouses","sushi bars","swabian","swedish","swiss food","tabernas","taiwanese","tapas bars","tapas/small plates","tex-mex","thai","traditional norwegian","traditional swedish","turkish","chee kufta","gozleme","turkish ravioli","ukrainian","vegan","vegetarian","venison","vietnamese","wok","wraps","yugoslav"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		else {
 			String[] toAdd = {"shopping","adult","antiques","art galleries","arts & crafts","art supplies","cards & stationery","costumes","embroidery & crochet","fabric stores","framing","auction houses","baby gear & furniture","bespoke clothing","books, mags, music & video","bookstores","comic books","music & dvds","newspapers & magazines","videos & video game rental","vinyl records","bridal","chinese bazaar","computers","concept shops","cosmetics & beauty supply","department stores","discount store","drugstores","electronics","eyewear & opticians","fashion","accessories","children's clothing","department stores","formal wear","hats","leather goods","lingerie","maternity wear","men's clothing","plus size fashion","shoe stores","sleepwear","sports wear","surf shop","swimwear","used, vintage & consignment","women's clothing","fireworks","flea markets","flowers & gifts","cards & stationery","florists","flowers","gift shops","golf equipment shops","guns & ammo","hobby shops","home & garden","appliances","furniture stores","hardware stores","home decor","hot tub & pool","kitchen & bath","linens","mattresses","nurseries & gardening","tableware","jewelry","kiosk","knitting supplies","luggage","market stalls","medical supplies","mobile phones","motorcycle gear","musical instruments & teachers","office equipment","outlet stores","pawn shops","perfume","personal shopping","photography stores & services","pop-up shops","scandinavian design","shopping centers","souvenir shops","spiritual shop","sporting goods","bikes","golf equipment","outdoor gear","sports wear","thrift stores","tickets","tobacco shops","toy stores","trophy shops","uniforms","used bookstore","watches","wholesale stores","wigs"};
 			groupMembers = addMyMembers(toAdd);
 		}
 		return groupMembers;
 	}
 
 
 	private ArrayList<Item> addMyMembers(String[] restaurantArray) {
 		ArrayList<Item> groupMembers = new ArrayList<Item>();
 		for(int i=0; i<restaurantArray.length; i++) {
 			Item item = new Item();
 			item.name = restaurantArray[i];
 			//TODO: Changing from here
 			//			item.id = restaurantArray[i];
 			item.id = new Integer(i).toString();
 			groupMembers.add(item);
 		}
 		return groupMembers; 
 	}
 }
