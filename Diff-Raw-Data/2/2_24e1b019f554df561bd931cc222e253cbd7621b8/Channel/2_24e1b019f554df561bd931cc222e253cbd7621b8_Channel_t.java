 package org.atlasapi.media.entity;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.model.SelfModelling;
 import com.metabroadcast.common.model.SimpleModel;
 
 public class Channel implements SelfModelling {
     // Change this and you have to rebuild the whole schedule index (if you still want to be able to do range queries
     public static final int MAX_KEY_LENGTH = 31;
 
     private static final String CHANNEL_URI_PREFIX = "http://ref.atlasapi.org/channels/";
     public static final Channel BBC_IPLAYER = new Channel("iPlayer", "http://www.bbc.co.uk/iplayer", "iplayer");
     public static final Channel HULU = new Channel("Hulu", "http://www.hulu.com", "hulu");
     public static final Channel YOUTUBE = new Channel("YouTube", "http://www.youtube.com", "youtube");
     public static final Channel SEESAW = new Channel("Seesaw", "http://www.seesaw.com", "seesaw");
     public static final Channel C4_4OD = new Channel("4oD", "http://www.channel4.com/programmes/4od", "4od");
     public static final Channel BBC_ONE = new Channel("BBC One", "http://www.bbc.co.uk/services/bbcone/london", "bbcone");
     public static final Channel BBC_ONE_NORTHERN_IRELAND = new Channel("BBC One Northern Ireland", "http://www.bbc.co.uk/services/bbcone/ni", "bbcone-ni");
     public static final Channel BBC_ONE_CAMBRIDGE = new Channel("BBC One Cambridgeshire", "http://www.bbc.co.uk/services/bbcone/cambridge", "bbcone-cambridge");
     public static final Channel BBC_ONE_CHANNEL_ISLANDS = new Channel("BBC One Channel Islands", "http://www.bbc.co.uk/services/bbcone/channel_islands", "bbcone-channel_islands");
     public static final Channel BBC_ONE_EAST = new Channel("BBC One East", "http://www.bbc.co.uk/services/bbcone/east", "bbcone-east");
     public static final Channel BBC_ONE_EAST_MIDLANDS = new Channel("BBC One East Midlands", "http://www.bbc.co.uk/services/bbcone/east_midlands", "bbcone-east_midlands");
     public static final Channel BBC_ONE_HD = new Channel("BBC One HD", "http://www.bbc.co.uk/services/bbcone/hd", "bbcone-hd");
     public static final Channel BBC_ONE_NORTH_EAST = new Channel("BBC One North East & Cumbria", "http://www.bbc.co.uk/services/bbcone/north_east", "bbcone-north_east");
     public static final Channel BBC_ONE_NORTH_WEST = new Channel("BBC One North West", "http://www.bbc.co.uk/services/bbcone/north_west", "bbcone-north_west");
     public static final Channel BBC_ONE_OXFORD = new Channel("BBC One Oxfordshire", "http://www.bbc.co.uk/services/bbcone/oxford", "bbcone-oxford");
     public static final Channel BBC_ONE_SCOTLAND = new Channel("BBC One Scotland", "http://www.bbc.co.uk/services/bbcone/scotland", "bbcone-scotland");
     public static final Channel BBC_ONE_SOUTH = new Channel("BBC One South", "http://www.bbc.co.uk/services/bbcone/south", "bbcone-south");
     public static final Channel BBC_ONE_SOUTH_EAST = new Channel("BBC One South East", "http://www.bbc.co.uk/services/bbcone/south_east", "bbcone-south_east");
     public static final Channel BBC_ONE_WALES = new Channel("BBC One Wales", "http://www.bbc.co.uk/services/bbcone/wales", "bbcone-wales");
     public static final Channel BBC_ONE_SOUTH_WEST = new Channel("BBC One South West", "http://www.bbc.co.uk/services/bbcone/south_west", "bbcone-south_west");
     public static final Channel BBC_ONE_WEST = new Channel("BBC One West", "http://www.bbc.co.uk/services/bbcone/west", "bbcone-west");
     public static final Channel BBC_ONE_WEST_MIDLANDS = new Channel("BBC One West Midlands", "http://www.bbc.co.uk/services/bbcone/west_midlands", "bbcone-west_midlands");
     public static final Channel BBC_ONE_EAST_YORKSHIRE = new Channel("BBC One Yorks & Lincs", "http://www.bbc.co.uk/services/bbcone/east_yorkshire", "bbcone-east_yorkshire");
     public static final Channel BBC_ONE_YORKSHIRE = new Channel("BBC One Yorkshire", "http://www.bbc.co.uk/services/bbcone/yorkshire", "bbcone-yorkshire");
     public static final Channel BBC_TWO = new Channel("BBC Two", "http://www.bbc.co.uk/services/bbctwo/england", "bbctwo");
 
     public static final Channel BBC_TWO_NORTHERN_IRELAND = new Channel("BBC Two Northern Ireland", "http://www.bbc.co.uk/services/bbctwo/ni", "bbctwo-ni");
     public static final Channel BBC_TWO_NORTHERN_IRELAND_ALALOGUE = new Channel("BBC Two Northern Ireland (Analogue)", "http://www.bbc.co.uk/services/bbctwo/ni_analogue", "bbctwo-ni_analogue");
     public static final Channel BBC_TWO_SCOTLAND = new Channel("BBC Two Scotland", "http://www.bbc.co.uk/services/bbctwo/scotland", "bbctwo-scotland");
     public static final Channel BBC_TWO_WALES = new Channel("BBC Two Wales", "http://www.bbc.co.uk/services/bbctwo/wales", "bbctwo-wales");
     public static final Channel BBC_TWO_WALES_ANALOGUE = new Channel("BBC Two Wales (Analogue)", "http://www.bbc.co.uk/services/bbctwo/wales_analogue", "bbctwo-walesanalogue");
 
     public static final Channel BBC_THREE = new Channel("BBC Three", "http://www.bbc.co.uk/services/bbcthree", "bbcthree");
     public static final Channel BBC_FOUR = new Channel("BBC Four", "http://www.bbc.co.uk/services/bbcfour", "bbcfour");
     public static final Channel BBC_NEWS = new Channel("BBC News", "http://www.bbc.co.uk/services/bbcnews", "bbcnews");
     public static final Channel BBC_WORLD_NEWS = new Channel("BBC World News", "http://www.bbc.co.uk/services/bbcworldnews", "bbcworldnews");
     public static final Channel BBC_PARLIMENT = new Channel("BBC Parliament", "http://www.bbc.co.uk/services/parliament", "bbcparliment");
     public static final Channel BBC_HD = new Channel("BBC HD", "http://www.bbc.co.uk/services/bbchd", "bbchd");
     public static final Channel CBBC = new Channel("CBBC", "http://www.bbc.co.uk/services/cbbc", "cbbc");
     public static final Channel CBEEBIES = new Channel("CBeebies", "http://www.bbc.co.uk/services/cbeebies", "cbeebies");
     
     public static final Channel BBC_RADIO_LONDON = new Channel("BBC Radio London", "http://www.bbc.co.uk/services/london", "london");
     public static final Channel BBC_RADIO_BERKSHIRE = new Channel("BBC Radio Berkshire", "http://www.bbc.co.uk/services/berkshire", "berkshire");
     public static final Channel BBC_RADIO_BRISTOL = new Channel("BBC Radio Bristol", "http://www.bbc.co.uk/services/bristol", "bristol");
     public static final Channel BBC_RADIO_CAMBRIDGESHIRE = new Channel("BBC Radio Cambridgeshire", "http://www.bbc.co.uk/services/cambridgeshire", "cambridgeshire");
     public static final Channel BBC_RADIO_CORNWALL = new Channel("BBC Radio Cornwall", "http://www.bbc.co.uk/services/cornwall", "cornwall");
     public static final Channel BBC_RADIO_COVENTRY = new Channel("BBC Radio Coventry", "http://www.bbc.co.uk/services/coventry", "coventry");
     public static final Channel BBC_RADIO_CUMBRIA = new Channel("BBC Radio Cumbria", "http://www.bbc.co.uk/services/cumbria", "cumbria");
     public static final Channel BBC_RADIO_DERBY = new Channel("BBC Radio Derby", "http://www.bbc.co.uk/services/derby", "derby");
     public static final Channel BBC_RADIO_DEVON = new Channel("BBC Radio Devon", "http://www.bbc.co.uk/services/devon", "devon");
     public static final Channel BBC_RADIO_ESSEX = new Channel("BBC Radio Essex", "http://www.bbc.co.uk/services/essex", "essex");
     public static final Channel BBC_RADIO_GLOUCESTERSHIRE = new Channel("BBC Radio Gloucestershire", "http://www.bbc.co.uk/services/gloucestershire", "gloucestershire");
     public static final Channel BBC_RADIO_GUERNSEY = new Channel("BBC Radio Guernsey", "http://www.bbc.co.uk/services/guernsey", "guernsey");
     public static final Channel BBC_RADIO_HEREFORDANDWORCESTER = new Channel("BBC Radio Herefordandworcester", "http://www.bbc.co.uk/services/herefordandworcester", "herefordandworcester");
     public static final Channel BBC_RADIO_HUMBERSIDE = new Channel("BBC Radio Humberside", "http://www.bbc.co.uk/services/humberside", "humberside");
     public static final Channel BBC_RADIO_JERSEY = new Channel("BBC Radio Jersey", "http://www.bbc.co.uk/services/jersey", "jersey");
     public static final Channel BBC_RADIO_KENT = new Channel("BBC Radio Kent", "http://www.bbc.co.uk/services/kent", "kent");
     public static final Channel BBC_RADIO_LANCASHIRE = new Channel("BBC Radio Lancashire", "http://www.bbc.co.uk/services/lancashire", "lancashire");
     public static final Channel BBC_RADIO_LEEDS = new Channel("BBC Radio Leeds", "http://www.bbc.co.uk/services/leeds", "leeds");
     public static final Channel BBC_RADIO_LEICESTER = new Channel("BBC Radio Leicester", "http://www.bbc.co.uk/services/leicester", "leicester");
     public static final Channel BBC_RADIO_LINCOLNSHIRE = new Channel("BBC Radio Lincolnshire", "http://www.bbc.co.uk/services/lincolnshire", "lincolnshire");
     public static final Channel BBC_RADIO_MANCHESTER = new Channel("BBC Radio Manchester", "http://www.bbc.co.uk/services/manchester", "manchester");
     public static final Channel BBC_RADIO_MERSEYSIDE = new Channel("BBC Radio Merseyside", "http://www.bbc.co.uk/services/merseyside", "merseyside");
     public static final Channel BBC_RADIO_NEWCASTLE = new Channel("BBC Radio Newcastle", "http://www.bbc.co.uk/services/newcastle", "newcastle");
     public static final Channel BBC_RADIO_NORFOLK = new Channel("BBC Radio Norfolk", "http://www.bbc.co.uk/services/norfolk", "norfolk");
     public static final Channel BBC_RADIO_NORTHAMPTON = new Channel("BBC Radio Northampton", "http://www.bbc.co.uk/services/northampton", "northampton");
     public static final Channel BBC_RADIO_NOTTINGHAM = new Channel("BBC Radio Nottingham", "http://www.bbc.co.uk/services/nottingham", "nottingham");
     public static final Channel BBC_RADIO_OXFORD = new Channel("BBC Radio Oxford", "http://www.bbc.co.uk/services/oxford", "oxford");
     public static final Channel BBC_RADIO_SHEFFIELD = new Channel("BBC Radio Sheffield", "http://www.bbc.co.uk/services/sheffield", "sheffield");
     public static final Channel BBC_RADIO_SHROPSHIRE = new Channel("BBC Radio Shropshire", "http://www.bbc.co.uk/services/shropshire", "shropshire");
     public static final Channel BBC_RADIO_SOLENT = new Channel("BBC Radio Solent", "http://www.bbc.co.uk/services/solent", "solent");
     public static final Channel BBC_RADIO_SOMERSET = new Channel("BBC Radio Somerset", "http://www.bbc.co.uk/services/somerset", "somerset");
     public static final Channel BBC_RADIO_STOKE = new Channel("BBC Radio Stoke", "http://www.bbc.co.uk/services/stoke", "stoke");
     public static final Channel BBC_RADIO_SUFFOLK = new Channel("BBC Radio Suffolk", "http://www.bbc.co.uk/services/suffolk", "suffolk");
     public static final Channel BBC_RADIO_SURREY = new Channel("BBC Radio Surrey", "http://www.bbc.co.uk/services/surrey", "surrey");
     public static final Channel BBC_RADIO_SUSSEX = new Channel("BBC Radio Sussex", "http://www.bbc.co.uk/services/sussex", "sussex");
     public static final Channel BBC_RADIO_WILTSHIRE = new Channel("BBC Radio Wiltshire", "http://www.bbc.co.uk/services/wiltshire", "wiltshire");
     public static final Channel BBC_RADIO_YORK = new Channel("BBC Radio York", "http://www.bbc.co.uk/services/york", "york");
     public static final Channel BBC_RADIO_TEES = new Channel("BBC Radio Tees", "http://www.bbc.co.uk/services/tees", "tees");
     public static final Channel BBC_RADIO_THREECOUNTIES = new Channel("BBC Radio Threecounties", "http://www.bbc.co.uk/services/threecounties", "threecounties");
     public static final Channel BBC_RADIO_WM = new Channel("BBC Radio Wm", "http://www.bbc.co.uk/services/wm", "wm");
     public static final Channel BBC_RADIO_RADIO1 = new Channel("BBC Radio Radio1", "http://www.bbc.co.uk/services/radio1/england", "radio1");
     public static final Channel BBC_RADIO_1XTRA = new Channel("BBC Radio 1xtra", "http://www.bbc.co.uk/services/1xtra", "1xtra");
     public static final Channel BBC_RADIO_RADIO2 = new Channel("BBC Radio Radio2", "http://www.bbc.co.uk/services/radio2", "radio2");
     public static final Channel BBC_RADIO_RADIO3 = new Channel("BBC Radio Radio3", "http://www.bbc.co.uk/services/radio3", "radio3");
     public static final Channel BBC_RADIO_RADIO4 = new Channel("BBC Radio Radio4", "http://www.bbc.co.uk/services/radio4/fm", "radio4");
     public static final Channel BBC_RADIO_5LIVE = new Channel("BBC Radio 5live", "http://www.bbc.co.uk/services/5live", "5live");
     public static final Channel BBC_RADIO_5LIVESPORTSEXTRA = new Channel("BBC Radio 5livesportsextra", "http://www.bbc.co.uk/services/5livesportsextra", "5livesportsextra");
     public static final Channel BBC_RADIO_6MUSIC = new Channel("BBC Radio 6music", "http://www.bbc.co.uk/services/6music", "6music");
     public static final Channel BBC_RADIO_RADIO7 = new Channel("BBC Radio Radio7", "http://www.bbc.co.uk/services/radio7", "radio7");
     public static final Channel BBC_RADIO_ASIANNETWORK = new Channel("BBC Radio Asiannetwork", "http://www.bbc.co.uk/services/asiannetwork", "asiannetwork");
     public static final Channel BBC_RADIO_WORLDSERVICE = new Channel("BBC Radio Worldservice", "http://www.bbc.co.uk/services/worldservice", "worldservice");
     public static final Channel BBC_RADIO_RADIOSCOTLAND = new Channel("BBC Radio Radioscotland", "http://www.bbc.co.uk/services/radioscotland/fm", "radioscotland");
     public static final Channel BBC_RADIO_RADIOSCOTLAND_MW = new Channel("BBC Radio Radioscotland MW", "http://www.bbc.co.uk/services/radioscotland/mw", "radioscotlandmw");
     public static final Channel BBC_RADIO_RADIONANGAIDHEAL = new Channel("BBC Radio Radionangaidheal", "http://www.bbc.co.uk/services/radionangaidheal", "radionangaidheal");
     public static final Channel BBC_RADIO_RADIOULSTER = new Channel("BBC Radio Radioulster", "http://www.bbc.co.uk/services/radioulster", "radioulster");
     public static final Channel BBC_RADIO_RADIOFOYLE = new Channel("BBC Radio Radiofoyle", "http://www.bbc.co.uk/services/radiofoyle", "radiofoyle");
     public static final Channel BBC_RADIO_RADIOWALES = new Channel("BBC Radio Radiowales", "http://www.bbc.co.uk/services/radiowales/fm", "radiowales");
     public static final Channel BBC_RADIO_RADIOORKNEY = new Channel("BBC Radio Radioorkney", "http://www.bbc.co.uk/services/radioorkney", "radioorkney");
     public static final Channel BBC_RADIO_RADIOSHETLAND = new Channel("BBC Radio Radioshetland", "http://www.bbc.co.uk/services/radioshetland", "radioshetland");
     public static final Channel BBC_RADIO_RADIOCYMRU = new Channel("BBC Radio Radiocymru", "http://www.bbc.co.uk/services/radiocymru", "radiocymru");
     public static final Channel BBC_RADIO_RADIO4_LW = new Channel("BBC Radio Radio4 LW", "http://www.bbc.co.uk/services/radio4/lw", "radio4lw");
     public static final Channel BBC_RADIO_RADIO4_EXTRA = new Channel("BBC Radio Radio4 Extra", "http://www.bbc.co.uk/services/radio4extra", "radio4extra");
 
     public static final Channel CHANNEL_FOUR = new Channel("Channel 4", "http://www.channel4.com", "channel4");
     public static final Channel MORE_FOUR = new Channel("More 4", "http://www.channel4.com/more4", "more4");
     public static final Channel FILM_4 = new Channel("Film 4", "http://film4.com", "film4");
     public static final Channel E_FOUR = new Channel("E4", "http://www.e4.com", "e4");
     public static final Channel FOUR_MUSIC = new Channel("4 Music", "http://www.4music.com", "4music");
 
     public static final Channel FIVE = new Channel("Five", "http://www.five.tv", "five");
     public static final Channel FIVE_PLUS1 = new Channel("Five +1", "http://www.five.tv/plus1", "fiveplus1");
     public static final Channel FIVE_HD = new Channel("Five HD", "http://www.five.tv/channels/five_hd", "fivehd");
     public static final Channel FIVER = new Channel("Fiver", "http://www.five.tv/channels/fiver", "fiver");
     public static final Channel FIVE_USA = new Channel("Five USA", "http://www.five.tv/channels/five-usa", "fiveusa");
 
     public static final Channel ITV1_LONDON = new Channel("ITV1 London", "http://www.itv.com/channels/itv1/london", "itv1london");
     public static final Channel ITV1_GRANADA = new Channel("ITV1 Granada", "http://www.itv.com/channels/itv1/granada", "itv1granada");
     public static final Channel ITV1_TYNE_TEES = new Channel("ITV1 Tyne Tees", "http://www.itv.com/channels/itv1/tynetees", "itv1tynetees");
     public static final Channel ITV1_BORDER_SOUTH = new Channel("ITV1 Border South", "http://www.itv.com/channels/itv1/bordersouth", "itv1bordersouth");
     public static final Channel ITV1_MERIDIAN = new Channel("ITV1 Meridian", "http://www.itv.com/channels/itv1/meridian", "itv1meridian");
     public static final Channel ITV1_ANGLIA = new Channel("ITV1 Anglia", "http://www.itv.com/channels/itv1/anglia", "itv1anglia");
     public static final Channel ITV1_CHANNEL = new Channel("ITV1 Channel", "http://www.itv.com/channels/itv1/channel", "itv1channel");
     public static final Channel ITV1_WALES = new Channel("ITV1 Wales", "http://www.itv.com/channels/itv1/wales", "itv1wales");
     public static final Channel ITV1_WEST = new Channel("ITV1 West", "http://www.itv.com/channels/itv1/west", "itv1west");
     public static final Channel ITV1_CARLTON_CENTRAL = new Channel("ITV1 Carlton-Central", "http://www.itv.com/channels/itv1/carltoncentral", "itv1carltoncentral");
     public static final Channel ITV1_CARLTON_WESTCOUNTRY = new Channel("ITV1 Carlton-Westcountry", "http://www.itv.com/channels/itv1/carltonwestcountry", "itv1carltonwestcountry");
     public static final Channel ITV1_BORDER_NORTH = new Channel("ITV1 Border North", "http://www.itv.com/channels/itv1/bordernorth", "itv1bordernorth");
     public static final Channel ITV1_THAMES_VALLEY_NORTH = new Channel("ITV1 Thames Valley North", "http://www.itv.com/channels/itv1/thamesvalleynorth", "itv1thamesvalleynorth");
     public static final Channel ITV1_THAMES_VALLEY_SOUTH = new Channel("ITV1 Thames Valley South", "http://www.itv.com/channels/itv1/thamesvalleysouth", "itv1thamesvalleysouth");
     public static final Channel ITV1_HD = new Channel("ITV1 HD", "http://www.itv.com/channels/itv1/hd", "itv1hd");
     public static final Channel ITV1_CENTRAL_PLUS1 = new Channel("ITV1 Central +1", "http://www.itv.com/channels/itv1/central#plus1", "itv1centralplus1");
     public static final Channel ITV1_GRANADA_PLUS1 = new Channel("ITV1 Granada +1", "http://www.itv.com/channels/itv1/granada#plus1", "itv1granadaplus1");
     public static final Channel ITV1_LONDON_PLUS1 = new Channel("ITV1 London +1", "http://www.itv.com/channels/itv1/london#plus1", "itv1londonplus1");
     public static final Channel ITV1_SOUTH_EAST_PLUS1 = new Channel("ITV1 South East +1", "http://www.itv.com/channels/itv1/southeast#plus1", "itv1southeastplus1");
     public static final Channel ITV1_UTV_PLUS1 = new Channel("ITV1 UTV +1", "http://www.itv.com/channels/itv1/utv#plus1", "itv1utvplus1");
     public static final Channel ITV1_WEST_PLUS1 = new Channel("ITV1 West +1", "http://www.itv.com/channels/itv1/west#plus1", "itv1westplus1");
     public static final Channel ITV1_YORKSHIRE_TYNE_TEES_PLUS1 = new Channel("ITV1 Yorkshire Tyne Tees +1", "http://www.itv.com/channels/itv1/yorkshiretynestees#plus1", "itv1yorkshiretyneteesplus1");
  
     public static final Channel ITV2 = new Channel("ITV2", "http://www.itv.com/channels/itv2", "itv2");
     public static final Channel ITV2_HD = new Channel("ITV2 HD", "http://www.itv.com/channels/itv2/hd", "itv2hd");
     public static final Channel ITV3 = new Channel("ITV3", "http://www.itv.com/channels/itv3", "itv3");
     public static final Channel ITV3_HD = new Channel("ITV3 HD", "http://www.itv.com/channels/itv3/hd", "itv3hd");
     public static final Channel ITV4 = new Channel("ITV4", "http://www.itv.com/channels/itv4", "itv4");
     public static final Channel ITV4_HD = new Channel("ITV4 HD", "http://www.itv.com/channels/itv4/hd", "itv4hd");
 
     public static final Channel YTV = new Channel("Y TV", CHANNEL_URI_PREFIX + "ytv", "ytv");
     public static final Channel ULSTER = new Channel("Ulster", CHANNEL_URI_PREFIX + "ulster", "ulster");
     public static final Channel ULSTER_HD = new Channel("Ulster HD", CHANNEL_URI_PREFIX + "ulsterhd", "ulsterhd");
     public static final Channel STV_CENTRAL = new Channel("STV Central", CHANNEL_URI_PREFIX + "stvcentral", "stvcentral");
     public static final Channel STV_HD = new Channel("SVT HD", CHANNEL_URI_PREFIX + "stvhd", "stvhd");
     public static final Channel STV_NORTH = new Channel("STV NORTH", CHANNEL_URI_PREFIX + "stvnorth", "stvnorth");
     public static final Channel Channel_4_PLUS1 = new Channel("Channel 4 +1", "http://www.channel4.com/cchannel4plus1", "channel4plus1");
     public static final Channel Channel_4_HD = new Channel("Channel 4 HD", CHANNEL_URI_PREFIX + "channel4hd", "channel4hd");
     public static final Channel S4C = new Channel("S4C", CHANNEL_URI_PREFIX + "s4c", "s4c");
     public static final Channel S4C_CLIRLUN = new Channel("S4C Clirlun", CHANNEL_URI_PREFIX + "s4cclirlun", "s4cclirlun");
     public static final Channel RTE1 = new Channel("RTE 1", CHANNEL_URI_PREFIX + "rte1", "rte1");
     public static final Channel RTE2 = new Channel("RTE 2", CHANNEL_URI_PREFIX + "rte2", "rte2");
     public static final Channel TG4 = new Channel("TG 4", CHANNEL_URI_PREFIX + "tg4", "tg4");
     public static final Channel TV3 = new Channel("TV 3", CHANNEL_URI_PREFIX + "tv3", "tv3");
     public static final Channel GMTV_DIGITAL = new Channel("GMTV Digital", CHANNEL_URI_PREFIX + "gmtv", "gmtv");
     public static final Channel ITV2_PLUS1 = new Channel("ITV2 +1", "http://www.itv.com/channels/itv2#plus1", "itv2plus1");
     public static final Channel E4_PLUS1 = new Channel("E4 +1", "http://www.e4.com/plus1", "e4plus1");
     public static final Channel E4_HD = new Channel("E4 HD", "http://www.e4.com/hd", "e4hd");
     public static final Channel ITV3_PLUS1 = new Channel("ITV3 +1", "http://www.itv.com/channels/itv3#plus1", "itv3plus1");
     public static final Channel MORE4_PLUS1 = new Channel("More4 +1", "http://www.channel4.com/more4#plus1", "more4plus1");
     public static final Channel ITV1_PLUS1 = new Channel("ITV1 +1", "http://www.itv.com/channels/itv1#plus1", "itv1plus1");
     public static final Channel ITV4_PLUS1 = new Channel("ITV4 +1", "http://www.itv.com/channels/itv4#plus1", "itv4plus1");
     public static final Channel CITV = new Channel("Children's ITV", "http://www.itv.com/channels/citv", "citv");
     public static final Channel FIVER_PLUS1 = new Channel("Fiver +1", "http://www.five.tv/channels/fiver#plus1", "fiverplus1");
     public static final Channel FIVER_USA_PLUS1 = new Channel("Fiver USA +1", "http://www.five.tv/channels/fiver-usa#plus1", "fiverusaplus1");
     public static final Channel THE_ADULT_CHANNEL = new Channel("The Adult Channel", CHANNEL_URI_PREFIX + "theadultchannel", "theadultchannel");
     public static final Channel MTV_HITS = new Channel("MTV Hits", CHANNEL_URI_PREFIX + "mtvhits", "mtvhits");
     public static final Channel MTV_BASE = new Channel("MTV Base", CHANNEL_URI_PREFIX + "mtvbase", "mtvbase");
     public static final Channel MTV = new Channel("MTV", CHANNEL_URI_PREFIX + "mtv", "mtv");
     public static final Channel MTV_PLUS1 = new Channel("MTV +1", CHANNEL_URI_PREFIX + "mtvplus1", "mtvplus1");
     public static final Channel TRAVELCHANNEL = new Channel("TRAVELCHANNEL", CHANNEL_URI_PREFIX + "travelchannel", "travelchannel");
     public static final Channel TRAVELCHANNEL_PLUS1 = new Channel("Travelchannel +1", CHANNEL_URI_PREFIX + "travelchannelplus1", "travelchannelplus1");
     public static final Channel TV5 = new Channel("TV5", CHANNEL_URI_PREFIX + "tv5", "tv5");
     public static final Channel ZEE_TV = new Channel("Zee TV", CHANNEL_URI_PREFIX + "zeetv", "zeetv");
     public static final Channel PHOENIX_CNE = new Channel("Phoenix Cne", CHANNEL_URI_PREFIX + "phoenixcne", "phoenixcne");
     public static final Channel CHALLENGE = new Channel("Challenge", CHANNEL_URI_PREFIX + "challenge", "challenge");
     public static final Channel CHALLENGE_PLUS1 = new Channel("Challenge +1", CHANNEL_URI_PREFIX + "challengeplus1", "challengeplus1");
     public static final Channel HOME = new Channel("HOME", CHANNEL_URI_PREFIX + "home", "home");
     public static final Channel HOME_PLUS1 = new Channel("Home +1", CHANNEL_URI_PREFIX + "homeplus1", "homeplus1");
     public static final Channel SKY_BOX_OFFICE_DIGITAL = new Channel("Sky Box Office Digital", CHANNEL_URI_PREFIX + "skyboxofficedigital", "skyboxofficedigital");
     public static final Channel BLOOMBERG_TV = new Channel("Bloomberg TV", CHANNEL_URI_PREFIX + "bloombergtv", "bloombergtv");
     public static final Channel THE_BOX = new Channel("The Box", CHANNEL_URI_PREFIX + "thebox", "thebox");
     public static final Channel CNN = new Channel("CNN", CHANNEL_URI_PREFIX + "cnn", "cnn");
     public static final Channel CARTOON_NETWORK = new Channel("Cartoon Network", CHANNEL_URI_PREFIX + "cartoonnetwork", "cartoonnetwork");
     public static final Channel GOD_CHANNEL = new Channel("God Channel", CHANNEL_URI_PREFIX + "godchannel", "godchannel");
     public static final Channel PLAYBOY_TV = new Channel("Playboy TV", CHANNEL_URI_PREFIX + "playboytv", "playboytv");
     public static final Channel ANIMAL_PLANET = new Channel("Animal Planet", CHANNEL_URI_PREFIX + "animalplanet", "animalplanet");
     public static final Channel ANIMAL_PLANET_PLUS1 = new Channel("Animal Planet +1", CHANNEL_URI_PREFIX + "animalplanetplus1", "animalplanetplus1");
     public static final Channel SKY_NEWS = new Channel("Sky News", CHANNEL_URI_PREFIX + "skynews", "skynews");
     public static final Channel SKY_NEWS_HD = new Channel("Sky News HD", CHANNEL_URI_PREFIX + "skynewshd", "skynewshd");
     public static final Channel MTV_ROCKS = new Channel("MTV Rocks", CHANNEL_URI_PREFIX + "mtvrocks", "mtvrocks");
     public static final Channel LIVING = new Channel("LIVING", CHANNEL_URI_PREFIX + "living", "living");
     public static final Channel LIVING_PLUS1 = new Channel("Living +1", CHANNEL_URI_PREFIX + "livingplus1", "livingplus1");
     public static final Channel LIVING_HD = new Channel("Living HD", CHANNEL_URI_PREFIX + "livinghd", "livinghd");
     public static final Channel BRAVO = new Channel("Bravo", CHANNEL_URI_PREFIX + "bravo", "bravo");
     public static final Channel BRAVO_PLUS1 = new Channel("Bravo +1", CHANNEL_URI_PREFIX + "bravoplus1", "bravoplus1");
     public static final Channel DISCOVERY_HISTORY = new Channel("Discovery History", CHANNEL_URI_PREFIX + "discoveryhistory", "discoveryhistory");
     public static final Channel DISCOVERY_HISTORY_PLUS_1 = new Channel("Discovery History +1", CHANNEL_URI_PREFIX + "discoveryhistoryplus1", "discoveryhistoryplus1");
     public static final Channel DISCOVERY_SCIENCE = new Channel("Discovery Science", CHANNEL_URI_PREFIX + "discoveryscience", "discoveryscience");
     public static final Channel DISCOVERY_SCIENCE_PLUS1 = new Channel("Discovery Science +1", CHANNEL_URI_PREFIX + "discoveryscienceplus1", "discoveryscienceplus1");
     public static final Channel DISCOVERY_TRAVEL_AND_LIVING = new Channel("Discovery Travel And Living", CHANNEL_URI_PREFIX + "discoverytravelandliving", "discoverytravelandliving");
     public static final Channel DISCOVERY_TRAVEL_AND_LIVING_PLUS1 = new Channel("Discovery Travel And Living +1", CHANNEL_URI_PREFIX + "discoverytravelandlivingplus1",
             "discoverytravelandlivingplus1");
     public static final Channel HISTORY = new Channel("History", CHANNEL_URI_PREFIX + "history", "history");
     public static final Channel HISTORY_PLUS1 = new Channel("History +1", CHANNEL_URI_PREFIX + "historyplus1", "historyplus1");
     public static final Channel NATIONAL_GEOGRAPHIC = new Channel("National Geographic", CHANNEL_URI_PREFIX + "nationalgeographic", "nationalgeographic");
     public static final Channel NATIONAL_GEOGRAPHIC_PLUS1 = new Channel("National Geographic +1", CHANNEL_URI_PREFIX + "nationalgeographicplus1", "nationalgeographicplus1");
     public static final Channel GOLD = new Channel("Gold", CHANNEL_URI_PREFIX + "gold", "gold");
     public static final Channel GOLD_PLUS1 = new Channel("Gold +1", CHANNEL_URI_PREFIX + "goldplus1", "goldplus1");
     public static final Channel THE_DISNEY_CHANNEL = new Channel("The Disney Channel", CHANNEL_URI_PREFIX + "thedisneychannel", "thedisneychannel");
     public static final Channel THE_DISNEY_CHANNEL_PLUS1 = new Channel("The Disney Channel +1", CHANNEL_URI_PREFIX + "thedisneychannelplus1", "thedisneychannelplus1");
     public static final Channel TELEVISION_X = new Channel("Television X", CHANNEL_URI_PREFIX + "televisionx", "televisionx");
     public static final Channel VH1 = new Channel("VH1", CHANNEL_URI_PREFIX + "vh1", "vh1");
     public static final Channel MTV_CLASSIC = new Channel("MTV Classic", CHANNEL_URI_PREFIX + "mtvclassic", "mtvclassic");
     public static final Channel DISCOVERY = new Channel("DISCOVERY", CHANNEL_URI_PREFIX + "discovery", "discovery");
     public static final Channel DISCOVERY_PLUS1 = new Channel("Discovery +1", CHANNEL_URI_PREFIX + "discoveryplus1", "discoveryplus1");
     public static final Channel DISCOVERY_PLUS1_POINT5 = new Channel("Discovery +1 Point5", CHANNEL_URI_PREFIX + "discoveryplus1point5", "discoveryplus1point5");
     public static final Channel DISCOVERY_TURBO = new Channel("Discovery Turbo", CHANNEL_URI_PREFIX + "discoveryturbo", "discoveryturbo");
     public static final Channel ALIBI = new Channel("Alibi", CHANNEL_URI_PREFIX + "alibi", "alibi");
     public static final Channel ALIBI_PLUS1 = new Channel("Alibi +1", CHANNEL_URI_PREFIX + "alibiplus1", "alibiplus1");
     public static final Channel UNIVERSAL = new Channel("Universal", CHANNEL_URI_PREFIX + "universal", "universal");
     public static final Channel UNIVERSAL_HD = new Channel("Universal HD", CHANNEL_URI_PREFIX + "universalhd", "universalhd");
     public static final Channel UNIVERSAL_PLUS1 = new Channel("Universal +1", CHANNEL_URI_PREFIX + "universalplus1", "universalplus1");
     public static final Channel SYFY = new Channel("SyFy", CHANNEL_URI_PREFIX + "syfy", "syfy");
     public static final Channel SYFY_PLUS1 = new Channel("SyFy +1", CHANNEL_URI_PREFIX + "syfyplus1", "syfyplus1");
     public static final Channel SYFY_HD = new Channel("SyFy HD", CHANNEL_URI_PREFIX + "syfyhd", "syfyhd");
     public static final Channel COMEDY_CENTRAL = new Channel("Comedy Central", CHANNEL_URI_PREFIX + "comedycentral", "comedycentral");
     public static final Channel COMEDY_CENTRAL_PLUS1 = new Channel("Comedy Central +1", CHANNEL_URI_PREFIX + "comedycentralplus1", "comedycentralplus1");
     public static final Channel SONY_ENTERTAINMENT_TV_ASIA = new Channel("Sony Entertainment TV Asia", CHANNEL_URI_PREFIX + "sonyentertainmenttvasia", "sonyentertainmenttvasia");
     public static final Channel TCM = new Channel("TCM", CHANNEL_URI_PREFIX + "tcm", "tcm");
     public static final Channel NICKELODEON = new Channel("Nickelodeon", CHANNEL_URI_PREFIX + "nickelodeon", "nickelodeon");
     public static final Channel NICKELODEON_REPLAY = new Channel("Nickelodeon Replay", CHANNEL_URI_PREFIX + "nickelodeonreplay", "nickelodeonreplay");
     public static final Channel CNBC = new Channel("CNBC", CHANNEL_URI_PREFIX + "cnbc", "cnbc");
     public static final Channel NICK_JR = new Channel("Nick Jr", CHANNEL_URI_PREFIX + "nickjr", "nickjr");
     public static final Channel BOOMERANG = new Channel("Boomerang", CHANNEL_URI_PREFIX + "boomerang", "boomerang");
     public static final Channel BOOMERANG_PLUS1 = new Channel("Boomerang +1", CHANNEL_URI_PREFIX + "boomerangplus1", "boomerangplus1");
     public static final Channel QVC = new Channel("QVC", CHANNEL_URI_PREFIX + "qvc", "qvc");
     public static final Channel GBC = new Channel("GBC", CHANNEL_URI_PREFIX + "gbc", "gbc");
     public static final Channel BBC_ENTERTAINMENT = new Channel("BBC Entertainment", CHANNEL_URI_PREFIX + "bbcentertainment", "bbcentertainment");
     public static final Channel FILM4_HD = new Channel("FILM4 HD", CHANNEL_URI_PREFIX + "film4hd", "film4hd");
     public static final Channel FILM4_PLUS1 = new Channel("FILM4 +1", CHANNEL_URI_PREFIX + "film4plus1", "film4plus1");
     public static final Channel SKY1 = new Channel("SKY1", CHANNEL_URI_PREFIX + "sky1", "sky1");
     public static final Channel DISCOVERY_HOME_AND_HEALTH = new Channel("Discovery Home And Health", CHANNEL_URI_PREFIX + "discoveryhomeandhealth", "discoveryhomeandhealth");
     public static final Channel DISCOVERY_HOME_AND_HEALTH_PLUS1 = new Channel("Discovery Home And Health +1", CHANNEL_URI_PREFIX + "discoveryhomeandhealthplus1",
             "discoveryhomeandhealthplus1");
     public static final Channel MUTV = new Channel("MUTV", CHANNEL_URI_PREFIX + "mutv", "mutv");
     public static final Channel SKY_SPORTS_NEWS = new Channel("Sky Sports News", CHANNEL_URI_PREFIX + "skysportsnews", "skysportsnews");
     public static final Channel EUROSPORT = new Channel("EUROSPORT", CHANNEL_URI_PREFIX + "eurosport", "eurosport");
     public static final Channel EUROSPORT_HD = new Channel("Eurosport HD", CHANNEL_URI_PREFIX + "eurosporthd", "eurosporthd");
     public static final Channel KISS = new Channel("KISS", CHANNEL_URI_PREFIX + "kiss", "kiss");
     public static final Channel PLAYHOUSE_DISNEY = new Channel("Playhouse Disney", CHANNEL_URI_PREFIX + "playhousedisney", "playhousedisney");
     public static final Channel PLAYHOUSE_DISNEY_PLUS = new Channel("Playhouse Disney Plus", CHANNEL_URI_PREFIX + "playhousedisneyplus", "playhousedisneyplus");
     public static final Channel SKY_SPORTS_1 = new Channel("Sky Sports 1", CHANNEL_URI_PREFIX + "skysports1", "skysports1");
     public static final Channel SKY_SPORTS_2 = new Channel("Sky Sports 2", CHANNEL_URI_PREFIX + "skysports2", "skysports2");
     public static final Channel SKY_SPORTS_3 = new Channel("Sky Sports 3", CHANNEL_URI_PREFIX + "skysports3", "skysports3");
     public static final Channel SKY_SPORTS_4 = new Channel("Sky Sports 4", CHANNEL_URI_PREFIX + "skysports4", "skysports4");
     public static final Channel BIO = new Channel("BIO", CHANNEL_URI_PREFIX + "bio", "bio");
     public static final Channel BID_TV = new Channel("Bid TV", CHANNEL_URI_PREFIX + "bidtv", "bidtv");
     public static final Channel SKY_ARTS_1 = new Channel("Sky Arts 1", CHANNEL_URI_PREFIX + "skyarts1", "skyarts1");
     public static final Channel EURONEWS = new Channel("EURONEWS", CHANNEL_URI_PREFIX + "euronews", "euronews");
     public static final Channel B4U_MOVIES = new Channel("B4U Movies", CHANNEL_URI_PREFIX + "b4umovies", "b4umovies");
     public static final Channel RED_HOT_AMATEUR = new Channel("Red Hot Amateur", CHANNEL_URI_PREFIX + "redhotamateur", "redhotamateur");
     public static final Channel EXTREME_SPORTS = new Channel("Extreme Sports", CHANNEL_URI_PREFIX + "extremesports", "extremesports");
     public static final Channel MTV_DANCE = new Channel("MTV Dance", CHANNEL_URI_PREFIX + "mtvdance", "mtvdance");
     public static final Channel STAR_PLUS = new Channel("Star Plus", CHANNEL_URI_PREFIX + "starplus", "starplus");
     public static final Channel TELEG = new Channel("TELEG", CHANNEL_URI_PREFIX + "teleg", "teleg");
     public static final Channel CHANNEL_9 = new Channel("Channel 9", CHANNEL_URI_PREFIX + "channel9", "channel9");
     public static final Channel GOOD_FOOD = new Channel("Good Food", CHANNEL_URI_PREFIX + "goodfood", "goodfood");
     public static final Channel GOOD_FOOD_PLUS1 = new Channel("Good Food +1", CHANNEL_URI_PREFIX + "goodfoodplus1", "goodfoodplus1");
     public static final Channel ATTHERACES = new Channel("ATTHERACES", CHANNEL_URI_PREFIX + "attheraces", "attheraces");
     public static final Channel NICKTOONS_TV = new Channel("Nicktoons TV", CHANNEL_URI_PREFIX + "nicktoonstv", "nicktoonstv");
     public static final Channel NICKTOONS_REPLAY = new Channel("Nicktoons Replay", CHANNEL_URI_PREFIX + "nicktoonsreplay", "nicktoonsreplay");
     public static final Channel CHART_SHOW_TV = new Channel("Chart Show TV", CHANNEL_URI_PREFIX + "chartshowtv", "chartshowtv");
     public static final Channel YESTERDAY = new Channel("YESTERDAY", CHANNEL_URI_PREFIX + "yesterday", "yesterday");
     public static final Channel YESTERDAY_PLUS1 = new Channel("Yesterday +1", CHANNEL_URI_PREFIX + "yesterdayplus1", "yesterdayplus1");
     public static final Channel MAGIC = new Channel("MAGIC", CHANNEL_URI_PREFIX + "magic", "magic");
     public static final Channel SMASH_HITS = new Channel("Smash Hits", CHANNEL_URI_PREFIX + "smashhits", "smashhits");
     public static final Channel KERRANG = new Channel("KERRANG", CHANNEL_URI_PREFIX + "kerrang", "kerrang");
     public static final Channel THE_COMMUNITY_CHANNEL = new Channel("The Community Channel", CHANNEL_URI_PREFIX + "thecommunitychannel", "thecommunitychannel");
     public static final Channel SKY2 = new Channel("SKY2", CHANNEL_URI_PREFIX + "sky2", "sky2");
     public static final Channel E_EXLAMATION = new Channel("E Exlamation", CHANNEL_URI_PREFIX + "eexlamation", "eexlamation");
     public static final Channel FLAUNT = new Channel("FLAUNT", CHANNEL_URI_PREFIX + "flaunt", "flaunt");
     public static final Channel SCUZZ = new Channel("SCUZZ", CHANNEL_URI_PREFIX + "scuzz", "scuzz");
     public static final Channel BLISS = new Channel("BLISS", CHANNEL_URI_PREFIX + "bliss", "bliss");
     public static final Channel ESPN_AMERICA = new Channel("Espn America", CHANNEL_URI_PREFIX + "espnamerica", "espnamerica");
     public static final Channel ESPN_AMERICA_HD = new Channel("Espn America HD", CHANNEL_URI_PREFIX + "espnamericahd", "espnamericahd");
     public static final Channel RED_HOT_40 = new Channel("Red Hot 40", CHANNEL_URI_PREFIX + "redhot40", "redhot40");
     public static final Channel DAVE = new Channel("DAVE", CHANNEL_URI_PREFIX + "dave", "dave");
     public static final Channel DAVE_JA_VU = new Channel("Dave Ja Vu", CHANNEL_URI_PREFIX + "davejavu", "davejavu");
     public static final Channel PRICE_DROP_TV = new Channel("Price Drop TV", CHANNEL_URI_PREFIX + "pricedroptv", "pricedroptv");
     public static final Channel FX = new Channel("FX", CHANNEL_URI_PREFIX + "fx", "fx");
     public static final Channel EDEN = new Channel("EDEN", CHANNEL_URI_PREFIX + "eden", "eden");
     public static final Channel EDEN_PLUS1 = new Channel("Eden +1", CHANNEL_URI_PREFIX + "edenplus1", "edenplus1");
     public static final Channel EDEN_HD = new Channel("Eden HD", CHANNEL_URI_PREFIX + "edenhd", "edenhd");
     public static final Channel BLIGHTY = new Channel("BLIGHTY", CHANNEL_URI_PREFIX + "blighty", "blighty");
     public static final Channel THE_HORROR_CHANNEL = new Channel("The Horror Channel", CHANNEL_URI_PREFIX + "thehorrorchannel", "thehorrorchannel");
     public static final Channel THE_HORROR_CHANNEL_PLUS1 = new Channel("The Horror Channel +1", CHANNEL_URI_PREFIX + "thehorrorchannelplus1", "thehorrorchannelplus1");
     public static final Channel RACING_UK = new Channel("Racing Uk", CHANNEL_URI_PREFIX + "racinguk", "racinguk");
     public static final Channel CHELSEA_TV = new Channel("Chelsea TV", CHANNEL_URI_PREFIX + "chelseatv", "chelseatv");
     public static final Channel STAR_NEWS = new Channel("Star News", CHANNEL_URI_PREFIX + "starnews", "starnews");
     public static final Channel SETANTA_IRELAND = new Channel("Setanta Ireland", CHANNEL_URI_PREFIX + "setantaireland", "setantaireland");
     public static final Channel LIVINGIT = new Channel("LIVINGIT", CHANNEL_URI_PREFIX + "livingit", "livingit");
     public static final Channel LIVINGIT_PLUS1 = new Channel("Livingit +1", CHANNEL_URI_PREFIX + "livingitplus1", "livingitplus1");
     public static final Channel EUROSPORT_2 = new Channel("Eurosport 2", CHANNEL_URI_PREFIX + "eurosport2", "eurosport2");
     public static final Channel REALLY = new Channel("REALLY", CHANNEL_URI_PREFIX + "really", "really");
     public static final Channel RED_HOT_TV = new Channel("Red Hot TV", CHANNEL_URI_PREFIX + "redhottv", "redhottv");
     public static final Channel COMEDY_CENTRAL_EXTRA = new Channel("Comedy Central Extra", CHANNEL_URI_PREFIX + "comedycentralextra", "comedycentralextra");
     public static final Channel COMEDY_CENTRAL_EXTRA_PLUS1 = new Channel("Comedy Central Extra +1", CHANNEL_URI_PREFIX + "comedycentralextraplus1", "comedycentralextraplus1");
     public static final Channel COMEDY_CENTRAL_HD = new Channel("Comedy Central HD", CHANNEL_URI_PREFIX + "comedycentralhd", "comedycentralhd");
     public static final Channel M95_TV_MARBELLA = new Channel("M95 TV Marbella", CHANNEL_URI_PREFIX + "m95tvmarbella", "m95tvmarbella");
     public static final Channel TV3_SPANISH = new Channel("TV3 Spanish", CHANNEL_URI_PREFIX + "tv3spanish", "tv3spanish");
     public static final Channel DISCOVERY_REAL_TIME = new Channel("Discovery Real Time", CHANNEL_URI_PREFIX + "discoveryrealtime", "discoveryrealtime");
     public static final Channel DISCOVERY_REAL_TIME_PLUS1 = new Channel("Discovery Real Time +1", CHANNEL_URI_PREFIX + "discoveryrealtimeplus1", "discoveryrealtimeplus1");
     public static final Channel MOTORS_TV = new Channel("Motors TV", CHANNEL_URI_PREFIX + "motorstv", "motorstv");
     public static final Channel DISCOVERY_SHED = new Channel("Discovery Shed", CHANNEL_URI_PREFIX + "discoveryshed", "discoveryshed");
     public static final Channel TRUE_MOVIES = new Channel("True Movies", CHANNEL_URI_PREFIX + "truemovies", "truemovies");
     public static final Channel SKY3 = new Channel("Pick TV", CHANNEL_URI_PREFIX + "sky3", "sky3");
     public static final Channel SKY_3_PLUS1 = new Channel("Pick TV +1", CHANNEL_URI_PREFIX + "sky3plus1", "sky3plus1");
     public static final Channel DISNEY_CINEMAGIC = new Channel("Disney Cinemagic", CHANNEL_URI_PREFIX + "disneycinemagic", "disneycinemagic");
     public static final Channel DISNEY_CINEMAGIC_PLUS1 = new Channel("Disney Cinemagic +1", CHANNEL_URI_PREFIX + "disneycinemagicplus1", "disneycinemagicplus1");
     public static final Channel DISNEY_CINEMAGIC_HD = new Channel("Disney Cinemagic HD", CHANNEL_URI_PREFIX + "disneycinemagichd", "disneycinemagichd");
     public static final Channel ESPN_CLASSIC = new Channel("Espn Classic", CHANNEL_URI_PREFIX + "espnclassic", "espnclassic");
     public static final Channel THREE_E = new Channel("Three E", CHANNEL_URI_PREFIX + "threee", "threee");
     public static final Channel FILMFLEX = new Channel("FILMFLEX", CHANNEL_URI_PREFIX + "filmflex", "filmflex");
     public static final Channel TCM2 = new Channel("TCM2", CHANNEL_URI_PREFIX + "tcm2", "tcm2");
     public static final Channel CHRISTMAS24 = new Channel("CHRISTMAS24", CHANNEL_URI_PREFIX + "christmas24", "christmas24");
     public static final Channel CHRISTMAS24_PLUS = new Channel("CHRISTMAS24 Plus", CHANNEL_URI_PREFIX + "christmas24plus", "christmas24plus");
     public static final Channel DISCOVERY_HD = new Channel("Discovery HD", CHANNEL_URI_PREFIX + "discoveryhd", "discoveryhd");
     public static final Channel NATIONAL_GEOGRAPHIC_HD = new Channel("National Geographic HD", CHANNEL_URI_PREFIX + "nationalgeographichd", "nationalgeographichd");
     public static final Channel SKY_BOX_OFFICE_HD1 = new Channel("Sky Box Office HD1", CHANNEL_URI_PREFIX + "skyboxofficehd1", "skyboxofficehd1");
     public static final Channel SKY_BOX_OFFICE_HD2 = new Channel("Sky Box Office HD2", CHANNEL_URI_PREFIX + "skyboxofficehd2", "skyboxofficehd2");
     public static final Channel SKY_SPORTS_1_HD = new Channel("Sky Sports 1 HD", CHANNEL_URI_PREFIX + "skysports1hd", "skysports1hd");
     public static final Channel SKY_SPORTS_2_HD = new Channel("Sky Sports 2 HD", CHANNEL_URI_PREFIX + "skysports2hd", "skysports2hd");
     public static final Channel E_EUROPE = new Channel("E Europe", CHANNEL_URI_PREFIX + "eeurope", "eeurope");
     public static final Channel NAT_GEO_WILD = new Channel("Nat Geo Wild", CHANNEL_URI_PREFIX + "natgeowild", "natgeowild");
     public static final Channel CRIME_AND_INVESTIGATION = new Channel("Crime And Investigation", CHANNEL_URI_PREFIX + "crimeandinvestigation", "crimeandinvestigation");
     public static final Channel CRIME_AND_INVESTIGATION_PLUS1 = new Channel("Crime And Investigation +1", CHANNEL_URI_PREFIX + "crimeandinvestigationplus1", "crimeandinvestigationplus1");
     public static final Channel SKY_MOVIES_CHRISTMAS_CHANNEL = new Channel("Sky Movies Christmas", CHANNEL_URI_PREFIX + "skymovieschristmas", "skymovieschristmas");
     public static final Channel SKY_MOVIES_CHRISTMAS_CHANNEL_HD = new Channel("Sky Movies Christmas HD", CHANNEL_URI_PREFIX + "skymovieschristmashd", "skymovieschristmashd");
     public static final Channel Channel = new Channel("Channel", CHANNEL_URI_PREFIX + "channel", "channel");
     public static final Channel Channel_HD = new Channel("Channel HD", CHANNEL_URI_PREFIX + "channelhd", "channelhd");
     public static final Channel SKY_MOVIES_CRIME_AND_THRILLER = new Channel("Sky Movies Crime And Thriller", CHANNEL_URI_PREFIX + "skymoviescrimeandthriller", "skymoviescrimeandthriller");
     public static final Channel SKY_MOVIES_CRIME_AND_THRILLER_HD = new Channel("Sky Movies Crime And Thriller HD", CHANNEL_URI_PREFIX + "skymoviescrimeandthrillerhd",
             "skymoviescrimeandthrillerhd");
     public static final Channel SKY_MOVIES_PREMIERE = new Channel("Sky Movies Premiere", CHANNEL_URI_PREFIX + "skymoviespremiere", "skymoviespremiere");
     public static final Channel SKY_MOVIES_PREMIERE_PLUS1 = new Channel("Sky Movies Premiere +1", CHANNEL_URI_PREFIX + "skymoviespremiereplus1", "skymoviespremiereplus1");
     public static final Channel SKY_MOVIES_PREMIERE_HD = new Channel("Sky Movies Premiere HD", CHANNEL_URI_PREFIX + "skymoviespremierehd", "skymoviespremierehd");
     public static final Channel SKY_MOVIES_COMEDY = new Channel("Sky Movies Comedy", CHANNEL_URI_PREFIX + "skymoviescomedy", "skymoviescomedy");
     public static final Channel SKY_MOVIES_COMEDY_HD = new Channel("Sky Movies Comedy HD", CHANNEL_URI_PREFIX + "skymoviescomedyhd", "skymoviescomedyhd");
     public static final Channel SKY_MOVIES_ACTION_AND_ADVENTURE = new Channel("Sky Movies Action And Adventure", CHANNEL_URI_PREFIX + "skymoviesactionandadventure",
             "skymoviesactionandadventure");
     public static final Channel SKY_MOVIES_ACTION_AND_ADVENTURE_HD = new Channel("Sky Movies Action And Adventure HD", CHANNEL_URI_PREFIX + "skymoviesactionandadventurehd",
             "skymoviesactionandadventurehd");
     public static final Channel SKY_MOVIES_FAMILY = new Channel("Sky Movies Family", CHANNEL_URI_PREFIX + "skymoviesfamily", "skymoviesfamily");
     public static final Channel SKY_MOVIES_FAMILY_HD = new Channel("Sky Movies Family HD", CHANNEL_URI_PREFIX + "skymoviesfamilyhd", "skymoviesfamilyhd");
     public static final Channel SKY_MOVIES_DRAMA_AND_ROMANCE = new Channel("Sky Movies Drama And Romance", CHANNEL_URI_PREFIX + "skymoviesdramaandromance", "skymoviesdramaandromance");
     public static final Channel SKY_MOVIES_DRAMA_AND_ROMANCE_HD = new Channel("Sky Movies Drama And Romance HD", CHANNEL_URI_PREFIX + "skymoviesdramaandromancehd",
             "skymoviesdramaandromancehd");
     public static final Channel SKY_MOVIES_SCIFI_HORROR = new Channel("Sky Movies Scifi Horror", CHANNEL_URI_PREFIX + "skymoviesscifihorror", "skymoviesscifihorror");
     public static final Channel SKY_MOVIES_SCIFI_HORROR_HD = new Channel("Sky Movies Scifi Horror HD", CHANNEL_URI_PREFIX + "skymoviesscifihorrorhd", "skymoviesscifihorrorhd");
     public static final Channel SKY_MOVIES_CLASSICS = new Channel("Sky Movies Classics", CHANNEL_URI_PREFIX + "skymoviesclassics", "skymoviesclassics");
     public static final Channel SKY_MOVIES_CLASSICS_HD = new Channel("Sky Movies Classics HD", CHANNEL_URI_PREFIX + "skymoviesclassicshd", "skymoviesclassicshd");
     public static final Channel SKY_MOVIES_MODERN_GREATS = new Channel("Sky Movies Modern Greats", CHANNEL_URI_PREFIX + "skymoviesmoderngreats", "skymoviesmoderngreats");
     public static final Channel SKY_MOVIES_MODERN_GREATS_HD = new Channel("Sky Movies Modern Greats HD", CHANNEL_URI_PREFIX + "skymoviesmoderngreatshd", "skymoviesmoderngreatshd");
     public static final Channel SKY_MOVIES_INDIE = new Channel("Sky Movies Indie", CHANNEL_URI_PREFIX + "skymoviesindie", "skymoviesindie");
     public static final Channel SKY_MOVIES_INDIE_HD = new Channel("Sky Movies Indie HD", CHANNEL_URI_PREFIX + "skymoviesindiehd", "skymoviesindiehd");
     public static final Channel AL_JAZEERA_ENGLISH = new Channel("Al Jazeera English", CHANNEL_URI_PREFIX + "aljazeeraenglish", "aljazeeraenglish");
     public static final Channel HISTORY_HD = new Channel("History HD", CHANNEL_URI_PREFIX + "historyhd", "historyhd");
     public static final Channel SKY1_HD = new Channel("SKY1 HD", CHANNEL_URI_PREFIX + "sky1hd", "sky1hd");
     public static final Channel SKY_ARTS_1_HD = new Channel("Sky Arts 1 HD", CHANNEL_URI_PREFIX + "skyarts1hd", "skyarts1hd");
     public static final Channel CARTOONITO = new Channel("CARTOONITO", CHANNEL_URI_PREFIX + "cartoonito", "cartoonito");
     public static final Channel BEST_DIRECT = new Channel("Best Direct", CHANNEL_URI_PREFIX + "bestdirect", "bestdirect");
     public static final Channel BRAVO_2 = new Channel("Bravo 2", CHANNEL_URI_PREFIX + "bravo2", "bravo2");
     public static final Channel DEUTSCHE_WELLE = new Channel("Deutsche Welle", CHANNEL_URI_PREFIX + "deutschewelle", "deutschewelle");
     public static final Channel GEMS_TV = new Channel("Gems TV", CHANNEL_URI_PREFIX + "gemstv", "gemstv");
     public static final Channel GEM_COLLECTOR = new Channel("Gem Collector", CHANNEL_URI_PREFIX + "gemcollector", "gemcollector");
     public static final Channel Channel_S = new Channel("Channel S", CHANNEL_URI_PREFIX + "channels", "channels");
     public static final Channel SETANTA_SPORTS_1_IRELAND = new Channel("Setanta Sports 1 Ireland", CHANNEL_URI_PREFIX + "setantasports1ireland", "setantasports1ireland");
     public static final Channel DIVA = new Channel("Diva", CHANNEL_URI_PREFIX + "diva", "diva");
     public static final Channel DIVA_PLUS1 = new Channel("Diva +1", CHANNEL_URI_PREFIX + "divaplus1", "divaplus1");
     public static final Channel Channel_ONE = new Channel("Channel One", CHANNEL_URI_PREFIX + "channelone", "channelone");
     public static final Channel Channel_ONE_PLUS1 = new Channel("Channel One +1", CHANNEL_URI_PREFIX + "channeloneplus1", "channeloneplus1");
     public static final Channel CN_TOO = new Channel("CN Too", CHANNEL_URI_PREFIX + "cntoo", "cntoo");
     public static final Channel POP = new Channel("POP", CHANNEL_URI_PREFIX + "pop", "pop");
     public static final Channel TINY_POP = new Channel("Tiny Pop", CHANNEL_URI_PREFIX + "tinypop", "tinypop");
     public static final Channel DMAX = new Channel("DMAX", CHANNEL_URI_PREFIX + "dmax", "dmax");
     public static final Channel DMAX_PLUS1 = new Channel("DMAX +1", CHANNEL_URI_PREFIX + "dmaxplus1", "dmaxplus1");
     public static final Channel DMAX_2 = new Channel("DMAX 2", CHANNEL_URI_PREFIX + "dmax2", "dmax2");
     public static final Channel HORSE_AND_COUNTRY = new Channel("Horse And Country", CHANNEL_URI_PREFIX + "horseandcountry", "horseandcountry");
     public static final Channel Channel_7 = new Channel("Channel 7", CHANNEL_URI_PREFIX + "channel7", "channel7");
     public static final Channel SKY_SPORTS_3_HD = new Channel("Sky Sports 3 HD", CHANNEL_URI_PREFIX + "skysports3hd", "skysports3hd");
     public static final Channel FLAVA = new Channel("Flava", CHANNEL_URI_PREFIX + "flava", "flava");
     public static final Channel FX_HD = new Channel("Fx HD", CHANNEL_URI_PREFIX + "fxhd", "fxhd");
     public static final Channel NATIONAL_GEOGRAPHIC_HD_PAN_EUROPEAN = new Channel("National Geographic HD Pan European", CHANNEL_URI_PREFIX + "nationalgeographichdpaneuropean",
             "nationalgeographichdpaneuropean");
     public static final Channel MOVIES4MEN = new Channel("MOVIES4MEN", CHANNEL_URI_PREFIX + "movies4men", "movies4men");
     public static final Channel MOVIES4MEN_PLUS1 = new Channel("MOVIES4Men +1", CHANNEL_URI_PREFIX + "movies4menplus1", "movies4menplus1");
     public static final Channel TRUE_MOVIES_2 = new Channel("True Movies 2", CHANNEL_URI_PREFIX + "truemovies2", "truemovies2");
     public static final Channel MOVIES4MEN2 = new Channel("MOVIES4MEN2", CHANNEL_URI_PREFIX + "movies4men2", "movies4men2");
     public static final Channel MOVIES4MEN2_PLUS1 = new Channel("MOVIES4MEN2 +1", CHANNEL_URI_PREFIX + "movies4men2plus1", "movies4men2plus1");
     public static final Channel MILITARY_HISTORY = new Channel("Military History", CHANNEL_URI_PREFIX + "militaryhistory", "militaryhistory");
     public static final Channel THE_STYLE_NETWORK = new Channel("The Style Network", CHANNEL_URI_PREFIX + "thestylenetwork", "thestylenetwork");
     public static final Channel WATCH = new Channel("Watch", CHANNEL_URI_PREFIX + "watch", "watch");
     public static final Channel WATCH_PLUS1 = new Channel("Watch +1", CHANNEL_URI_PREFIX + "watchplus1", "watchplus1");
     public static final Channel SKY_ARTS_2 = new Channel("Sky Arts 2", CHANNEL_URI_PREFIX + "skyarts2", "skyarts2");
     public static final Channel WEDDING_TV = new Channel("Wedding TV", CHANNEL_URI_PREFIX + "weddingtv", "weddingtv");
     public static final Channel PROPELLER_TV = new Channel("Propeller TV", CHANNEL_URI_PREFIX + "propellertv", "propellertv");
     public static final Channel MTVN_HD = new Channel("MTVn HD", CHANNEL_URI_PREFIX + "mtvnhd", "mtvnhd");
     public static final Channel INVESTIGATION_DISCOVERY = new Channel("Investigation Discovery", CHANNEL_URI_PREFIX + "investigationdiscovery", "investigationdiscovery");
     public static final Channel AIT_INTERNATIONAL = new Channel("Ait International", CHANNEL_URI_PREFIX + "aitinternational", "aitinternational");
     public static final Channel CINEMOI = new Channel("CINEMOI", CHANNEL_URI_PREFIX + "cinemoi", "cinemoi");
     public static final Channel SKY_ARTS_2_HD = new Channel("Sky Arts 2 HD", CHANNEL_URI_PREFIX + "skyarts2hd", "skyarts2hd");
     public static final Channel BIO_HD = new Channel("Bio HD", CHANNEL_URI_PREFIX + "biohd", "biohd");
     public static final Channel CRIME_AND_INVESTIGATION_HD = new Channel("Crime And Investigation HD", CHANNEL_URI_PREFIX + "crimeandinvestigationhd", "crimeandinvestigationhd");
     public static final Channel NAT_GEO_WILD_HD = new Channel("Nat Geo Wild HD", CHANNEL_URI_PREFIX + "natgeowildhd", "natgeowildhd");
     public static final Channel QUEST = new Channel("QUEST", CHANNEL_URI_PREFIX + "quest", "quest");
     public static final Channel DISCOVERY_QUEST_PLUS1 = new Channel("Discovery Quest +1", CHANNEL_URI_PREFIX + "discoveryquestplus1", "discoveryquestplus1");
     public static final Channel QUEST_FREEVIEW = new Channel("Quest Freeview", CHANNEL_URI_PREFIX + "questfreeview", "questfreeview");
     public static final Channel ESPN = new Channel("ESPN", CHANNEL_URI_PREFIX + "espn", "espn");
     public static final Channel ESPN_HD = new Channel("ESPN HD", CHANNEL_URI_PREFIX + "espnhd", "espnhd");
     public static final Channel DISNEY_XD = new Channel("Disney Xd", CHANNEL_URI_PREFIX + "disneyxd", "disneyxd");
     public static final Channel DISNEY_XD_PLUS1 = new Channel("Disney Xd +1", CHANNEL_URI_PREFIX + "disneyxdplus1", "disneyxdplus1");
     public static final Channel DISNEY_XD_HD = new Channel("Disney Xd HD", CHANNEL_URI_PREFIX + "disneyxdhd", "disneyxdhd");
     public static final Channel CBS_REALITY = new Channel("CBS Reality", CHANNEL_URI_PREFIX + "cbsreality", "cbsreality");
     public static final Channel MGM = new Channel("MGM", CHANNEL_URI_PREFIX + "mgm", "mgm");
     public static final Channel CBS_DRAMA = new Channel("CBS Drama", CHANNEL_URI_PREFIX + "cbsdrama", "cbsdrama");
     public static final Channel CBS_ACTION = new Channel("CBS Action", CHANNEL_URI_PREFIX + "cbsaction", "cbsaction");
     public static final Channel VIVA = new Channel("VIVA", CHANNEL_URI_PREFIX + "viva", "viva");
     public static final Channel FOOD_NETWORK = new Channel("Food Network", CHANNEL_URI_PREFIX + "foodnetwork", "foodnetwork");
     public static final Channel FOOD_NETWORK_PLUS1 = new Channel("Food Network +1", CHANNEL_URI_PREFIX + "foodnetworkplus1", "foodnetworkplus1");
     public static final Channel MGM_HD = new Channel("Mgm HD", CHANNEL_URI_PREFIX + "mgmhd", "mgmhd");
     public static final Channel MTV_SHOWS = new Channel("MTV Shows", CHANNEL_URI_PREFIX + "mtvshows", "mtvshows");
     public static final Channel NICK_JR_2 = new Channel("Nick Jr 2", CHANNEL_URI_PREFIX + "nickjr2", "nickjr2");
     public static final Channel NHK_WORLD = new Channel("NHK World", CHANNEL_URI_PREFIX + "nhkworld", "nhkworld");
     public static final Channel TRUEENT = new Channel("TRUEENT", CHANNEL_URI_PREFIX + "trueent", "trueent");
     public static final Channel BODY_IN_BALANCE = new Channel("Body In Balance", CHANNEL_URI_PREFIX + "bodyinbalance", "bodyinbalance");
     public static final Channel THE_ACTIVE_CHANNEL = new Channel("The Active Channel", CHANNEL_URI_PREFIX + "theactivechannel", "theactivechannel");
     public static final Channel FITNESS_TV = new Channel("Fitness TV", CHANNEL_URI_PREFIX + "fitnesstv", "fitnesstv");
     public static final Channel RUSH_HD = new Channel("Rush HD", CHANNEL_URI_PREFIX + "rushhd", "rushhd");
     public static final Channel BBC_SPORT_INTERACTIVE_BBC_ONE = new Channel("BBC Sport Interactive BBC One", CHANNEL_URI_PREFIX + "bbcsportinteractivebbcone", "bbcsportinteractivebbcone");
     public static final Channel BBC_SPORT_INTERACTIVE_BBC_TWO = new Channel("BBC Sport Interactive BBC Two", CHANNEL_URI_PREFIX + "bbcsportinteractivebbctwo", "bbcsportinteractivebbctwo");
     public static final Channel BBC_SPORT_INTERACTIVE_BBC_THREE = new Channel("BBC Sport Interactive BBC Three", CHANNEL_URI_PREFIX + "bbcsportinteractivebbcthree", "bbcsportinteractivebbcthree");
     public static final Channel BBC_SPORT_INTERACTIVE_FREEVIEW = new Channel("BBC Sport Interactive Freeview", CHANNEL_URI_PREFIX + "bbcsportinteractivefreeview",
             "bbcsportinteractivefreeview");
     public static final Channel SKY_3D = new Channel("Sky 3D", CHANNEL_URI_PREFIX + "sky3d", "sky3d");
     public static final Channel SKY_SPORTS_4_HD = new Channel("Sky Sports 4 HD", CHANNEL_URI_PREFIX + "skysports4hd", "skysports4hd");
     
     public static final Channel SKY_ATLANTIC = new Channel("Sky Atlantic", CHANNEL_URI_PREFIX+"skyatlantic", "skyatlantic");
     public static final Channel SKY_ATLANTIC_HD = new Channel("Sky Atlantic HD", CHANNEL_URI_PREFIX+"skyatlantichd", "skyatlantichd");
     public static final Channel SKY_MOVIES_SHOWCASE = new Channel("Sky Movies Showcase", CHANNEL_URI_PREFIX+"skymoviesshowcase", "skymoviesshowcase");
     public static final Channel SKY_MOVIES_SHOWCASE_HD = new Channel("Sky Movies Showcase HD", CHANNEL_URI_PREFIX+"skymoviesshowcasehd", "skymoviesshowcasehd");
     public static final Channel DISCOVERY_KNOWLEDGE = new Channel("Discovery Knowledge", CHANNEL_URI_PREFIX+"discoveryknowledge", "discoveryknowledge");
     public static final Channel SKY_LIVING = new Channel("Sky Living", CHANNEL_URI_PREFIX+"skyliving", "skyliving");
     public static final Channel SKY_LIVING_LOVES = new Channel("Sky Living Loves", CHANNEL_URI_PREFIX+"skylivingloves", "skylivingloves");
     public static final Channel LIVING_PLUS2 = new Channel("Living +2", CHANNEL_URI_PREFIX+"livingplus2", "livingplus2");
     public static final Channel FX_PLUS = new Channel("FX+", CHANNEL_URI_PREFIX+"fxplus", "fxplus");
     public static final Channel BBC_ALBA = new Channel("BBC Alba", CHANNEL_URI_PREFIX+"bbcalba", "bbcalba");
     public static final Channel SKY_LIVINGIT = new Channel("Sky Livingit", CHANNEL_URI_PREFIX+"skylivingit", "skylivingit");
     public static final Channel S4C2 = new Channel("S4C2", CHANNEL_URI_PREFIX+"sc42", "sc42");
     public static final Channel FIVE_USA_PLUS1 = new Channel("Five USA +1", CHANNEL_URI_PREFIX+"fiveusaplus1", "fiveusaplus1");
     public static final Channel MTV_MUSIC = new Channel("MTV Music", CHANNEL_URI_PREFIX+"mtvmusic", "mtvmusic");
     public static final Channel TVE_INTERNACIONAL = new Channel("TVE Internacional", CHANNEL_URI_PREFIX+"tveinternacional", "tveinternacional");
     public static final Channel SUPER_CASINO = new Channel("Super Casino", CHANNEL_URI_PREFIX+"supercasino", "supercasino");
     public static final Channel SIMPLY_SHOPPING = new Channel("Simply Shopping", CHANNEL_URI_PREFIX+"simplyshopping", "simplyshopping");
     public static final Channel Q = new Channel("Q", CHANNEL_URI_PREFIX+"q", "q");
     public static final Channel MOVIES_24 = new Channel("Movies24", CHANNEL_URI_PREFIX+"movies24", "movies24");
     public static final Channel MOVIES_24_PLUS = new Channel("Movies24+", CHANNEL_URI_PREFIX+"movies24plus", "movies24plus");
     public static final Channel BET_PLUS1 = new Channel("BET +1", CHANNEL_URI_PREFIX+"betplus1", "betplus1");
     public static final Channel BET_INTERNATIONAL = new Channel("BET International", CHANNEL_URI_PREFIX+"betinternational", "betinternational");
     public static final Channel PBS = new Channel("PBS", CHANNEL_URI_PREFIX + "pbs", "pbs");
     
     public static final Channel XFM_RADIO = new Channel("XFM", CHANNEL_URI_PREFIX + "xfmradio", "xfmradio");
     public static final Channel ABSOLUTE_RADIO = new Channel("Absolute Radio", CHANNEL_URI_PREFIX + "absoluteradio", "absoluteradio");
     public static final Channel ABSOLUTE_80S_RADIO = new Channel("Absolute 80s", CHANNEL_URI_PREFIX + "absolute80sradio", "absolute80sradio");
     public static final Channel ABSOLUTE_90S_RADIO = new Channel("Absolute 90s", CHANNEL_URI_PREFIX + "absolute90sradio", "absolute90sradio");
     public static final Channel ABSOLUTE_00S_RADIO = new Channel("Absolute 00s", CHANNEL_URI_PREFIX + "absolute00sradio", "absolute00sradio");
     public static final Channel ABSOLUTE_CLASSIC_ROCK_RADIO = new Channel("Absolute Classic Rock", CHANNEL_URI_PREFIX + "absoluteclassicrockradio", "absoluteclassicrockradio");
     public static final Channel ABSOLUTE_RADIO_EXTRA_RADIO = new Channel("Absolute Radio Extra", CHANNEL_URI_PREFIX + "absoluteradioextra", "absoluteradioextra");
     public static final Channel KISS_RADIO = new Channel("XFM", CHANNEL_URI_PREFIX + "kissradio", "kissradio");
     public static final Channel MAGIC_RADIO = new Channel("Magic", CHANNEL_URI_PREFIX + "magicradio", "magicradio");
     public static final Channel CLASSIC_FM_RADIO = new Channel("Classic FM", CHANNEL_URI_PREFIX + "classicfm", "classicfm");
     public static final Channel TALKSPORT_RADIO = new Channel("Talk Sport", CHANNEL_URI_PREFIX + "talksportradio", "talksportradio");
     public static final Channel SMASH_HITS_RADIO = new Channel("Smash Hits", CHANNEL_URI_PREFIX + "smashhitsradio", "smashhitsradio");
     public static final Channel KERRANG_RADIO = new Channel("Kerrang!", CHANNEL_URI_PREFIX + "kerrangradio", "kerrangradio");
     public static final Channel HEAT_RADIO = new Channel("Heat", CHANNEL_URI_PREFIX + "heatradio", "heatradio");
     public static final Channel CAPITAL_FM = new Channel("Capital FM", CHANNEL_URI_PREFIX + "capitalfm", "capitalfm");
     public static final Channel HEART_LONDON_RADIO = new Channel("Heart London", CHANNEL_URI_PREFIX + "heartlondonradio", "heartlondonradio");
     
     public final static Channel TVBLOB_7GOLD = new Channel("7 Gold", "http://tvblob.com/channel/7gold", "7gold");
     public final static Channel TVBLOB_ANTENNATRE = new Channel("Antenna Tre", "http://tvblob.com/channel/antennatre", "antennatre");
     public final static Channel TVBLOB_BOING = new Channel("Boing", "http://tvblob.com/channel/boing", "boing");
     public final static Channel TVBLOB_CANALEITALIA3 = new Channel("Canale Italia 3", "http://tvblob.com/channel/canaleitalia3", "canaleitalia3");
     public final static Channel TVBLOB_CANALEITALIA83 = new Channel("Canale Italia 83", "http://tvblob.com/channel/canaleitalia83", "canaleitalia83");
     public final static Channel TVBLOB_CANALEITALIA84 = new Channel("Canale Italia 84", "http://tvblob.com/channel/canaleitalia84", "canaleitalia84");
     public final static Channel TVBLOB_CANALEITALIAMUSICA = new Channel("Canale Italia Musica", "http://tvblob.com/channel/canaleitaliamusica", "canaleitaliamusica");
     public final static Channel TVBLOB_CANALEITALIAUNO = new Channel("Canale Italia Uno", "http://tvblob.com/channel/canaleitaliauno", "canaleitaliauno");
     public final static Channel TVBLOB_CANALE_5 = new Channel("Canale5", "http://tvblob.com/channel/canale_5", "canale_5");
     public final static Channel TVBLOB_CANALE5_PIU1 = new Channel("Canale5 +1", "http://tvblob.com/channel/canale5_piu1", "canale5_piu1");
     public final static Channel TVBLOB_CANALE5_HD = new Channel("Canale5 HD", "http://tvblob.com/channel/canale5_hd", "canale5_hd");
     public final static Channel TVBLOB_CANALE6 = new Channel("Canale6", "http://tvblob.com/channel/canale6", "canale6");
     public final static Channel TVBLOB_CAPRISTORE = new Channel("Capri Store", "http://tvblob.com/channel/capristore", "capristore");
     public final static Channel TVBLOB_CARTOONNETWORK = new Channel("Cartoon Network - Premium", "http://tvblob.com/channel/cartoonnetwork", "tvblobcartoonnetwork");
     public final static Channel TVBLOB_CHANNEL01 = new Channel("Channel 01", "http://tvblob.com/channel/channel01", "channel01");
     public final static Channel TVBLOB_CHANNEL02 = new Channel("Channel 02", "http://tvblob.com/channel/channel02", "channel02");
     public final static Channel TVBLOB_CHANNEL03 = new Channel("Channel 03", "http://tvblob.com/channel/channel03", "channel03");
     public final static Channel TVBLOB_CIELO = new Channel("Cielo", "http://tvblob.com/channel/cielo", "cielo");
     public final static Channel TVBLOB_CLASSHORSETV = new Channel("Class HorseTV", "http://tvblob.com/channel/classhorsetv", "classhorsetv");
     public final static Channel TVBLOB_CLASSNEWS = new Channel("ClassNews msnbc", "http://tvblob.com/channel/classnews", "classnews");
     public final static Channel TVBLOB_COMING_SOON = new Channel("Coming Soon Television", "http://tvblob.com/channel/coming_soon", "coming_soon");
     public final static Channel TVBLOB_CUBOVISION = new Channel("Cubo Vision", "http://tvblob.com/channel/cubovision", "cubovision");
     public final static Channel TVBLOB_DAHLIA = new Channel("dahlia", "http://tvblob.com/channel/dahlia", "dahlia");
     public final static Channel TVBLOB_DAHLIA1CALCIO = new Channel("dahlia 1 calcio", "http://tvblob.com/channel/dahlia1calcio", "dahlia1calcio");
     public final static Channel TVBLOB_DAHLIA2ADULT = new Channel("dahlia 2 adult", "http://tvblob.com/channel/dahlia2adult", "dahlia2adult");
     public final static Channel TVBLOB_DAHLIA2CALCIO = new Channel("dahlia 2 calcio", "http://tvblob.com/channel/dahlia2calcio", "dahlia2calcio");
     public final static Channel TVBLOB_DAHLIA2SPORT = new Channel("dahlia 2 sport", "http://tvblob.com/channel/dahlia2sport", "dahlia2sport");
     public final static Channel TVBLOB_DAHLIA3ADULT = new Channel("dahlia 3 adult", "http://tvblob.com/channel/dahlia3adult", "dahlia3adult");
     public final static Channel TVBLOB_DAHLIA3CALCIO = new Channel("dahlia 3 calcio", "http://tvblob.com/channel/dahlia3calcio", "dahlia3calcio");
     public final static Channel TVBLOB_DAHLIA4CALCIO = new Channel("dahlia 4 calcio", "http://tvblob.com/channel/dahlia4calcio", "dahlia4calcio");
     public final static Channel TVBLOB_DAHLIA5CALCIO = new Channel("dahlia 5 calcio", "http://tvblob.com/channel/dahlia5calcio", "dahlia5calcio");
     public final static Channel TVBLOB_DAHLIAADULT = new Channel("dahlia adult", "http://tvblob.com/channel/dahliaadult", "dahliaadult");
     public final static Channel TVBLOB_DAHLIAADULTGAY = new Channel("dahlia adult gay", "http://tvblob.com/channel/dahliaadultgay", "dahliaadultgay");
     public final static Channel TVBLOB_DAHLIAEXPLORER = new Channel("dahlia explorer", "http://tvblob.com/channel/dahliaexplorer", "dahliaexplorer");
     public final static Channel TVBLOB_DAHLIAEXTRA = new Channel("dahlia extra", "http://tvblob.com/channel/dahliaextra", "dahliaextra");
     public final static Channel TVBLOB_DAHLIASPORT = new Channel("dahlia sport", "http://tvblob.com/channel/dahliasport", "dahliasport");
     public final static Channel TVBLOB_DAHLIAXTREME = new Channel("dahlia xtreme", "http://tvblob.com/channel/dahliaxtreme", "dahliaxtreme");
     public final static Channel TVBLOB_DEEJAYTV = new Channel("Deejay TV", "http://tvblob.com/channel/deejaytv", "deejaytv");
     public final static Channel TVBLOB_DEEJAYTV_PIU2 = new Channel("Deejay TV +2", "http://tvblob.com/channel/deejaytv_piu2", "deejaytv_piu2");
     public final static Channel TVBLOB_DISNEYCHANNEL = new Channel("Disney Channel - Premium", "http://tvblob.com/channel/disneychannel", "disneychannel");
     public final static Channel TVBLOB_DISNEYCHANNELPIUUNO = new Channel("Disney Channel+1 - Premium", "http://tvblob.com/channel/disneychannelpiuuno", "disneychannelpiuuno");
     public final static Channel TVBLOB_E21NETWORK1 = new Channel("E21 NETWORK 1", "http://tvblob.com/channel/e21network1", "e21network1");
     public final static Channel TVBLOB_ELITESHOPPING = new Channel("Elite Shopping", "http://tvblob.com/channel/eliteshopping", "eliteshopping");
     public final static Channel TVBLOB_ESPANSIONETV = new Channel("Espansione TV", "http://tvblob.com/channel/espansionetv", "espansionetv");
     public final static Channel TVBLOB_EURONEWS = new Channel("Euronews", "http://tvblob.com/channel/euronews", "tvblobeuronews");
     public final static Channel TVBLOB_FDAUDITORIUM = new Channel("FD Auditorium", "http://tvblob.com/channel/fdauditorium", "fdauditorium");
     public final static Channel TVBLOB_FDLEGGERA = new Channel("FD Leggera", "http://tvblob.com/channel/fdleggera", "fdleggera");
     public final static Channel TVBLOB_FRANCE24 = new Channel("France 24", "http://tvblob.com/channel/france24", "france24");
     public final static Channel TVBLOB_FRISBEE = new Channel("Frisbee", "http://tvblob.com/channel/frisbee", "frisbee");
     public final static Channel TVBLOB_GLAMOURPLUS = new Channel("Glamour Plus VM18", "http://tvblob.com/channel/glamourplus", "glamourplus");
     public final static Channel TVBLOB_GOLDTV = new Channel("Gold TV", "http://tvblob.com/channel/goldtv", "goldtv");
     public final static Channel TVBLOB_HIRO = new Channel("Hiro - Premium", "http://tvblob.com/channel/hiro", "hiro");
     public final static Channel TVBLOB_HOLIDAY = new Channel("Holiday", "http://tvblob.com/channel/holiday", "holiday");
     public final static Channel TVBLOB_INTERTV = new Channel("Inter TV", "http://tvblob.com/channel/intertv", "intertv");
     public final static Channel TVBLOB_IRIS = new Channel("Iris", "http://tvblob.com/channel/iris", "iris");
     public final static Channel TVBLOB_ITALIA8AL = new Channel("Italia 8 AL", "http://tvblob.com/channel/italia8al", "italia8al");
     public final static Channel TVBLOB_ITALIA8MI = new Channel("Italia 8 MI", "http://tvblob.com/channel/italia8mi", "italia8mi");
     public final static Channel TVBLOB_ITALIA8PRESTIGE = new Channel("Italia 8 Prestige", "http://tvblob.com/channel/italia8prestige", "italia8prestige");
     public final static Channel TVBLOB_ITALIAMIA = new Channel("Italia Mia", "http://tvblob.com/channel/italiamia", "italiamia");
     public final static Channel TVBLOB_ITALIATV = new Channel("Italia TV", "http://tvblob.com/channel/italiatv", "italiatv");
     public final static Channel TVBLOB_ITALIA_1 = new Channel("Italia1", "http://tvblob.com/channel/italia_1", "italia_1");
     public final static Channel TVBLOB_ITALIA1_PIU1 = new Channel("Italia1 +1", "http://tvblob.com/channel/italia1_piu1", "italia1_piu1");
     public final static Channel TVBLOB_ITALIA1_HD = new Channel("Italia1 HD", "http://tvblob.com/channel/italia1_hd", "italia1_hd");
     public final static Channel TVBLOB_JOI = new Channel("Joi - Premium", "http://tvblob.com/channel/joi", "joi");
     public final static Channel TVBLOB_JOIPIUUNO = new Channel("Joi+1 - Premium", "http://tvblob.com/channel/joipiuuno", "joipiuuno");
     public final static Channel TVBLOB_K2 = new Channel("K2", "http://tvblob.com/channel/k2", "k2");
     public final static Channel TVBLOB_LA5 = new Channel("La5", "http://tvblob.com/channel/la5", "la5");
     public final static Channel TVBLOB_LA6 = new Channel("La6", "http://tvblob.com/channel/la6", "la6");
     public final static Channel TVBLOB_LA7 = new Channel("La7", "http://tvblob.com/channel/la7", "la7");
     public final static Channel TVBLOB_LA7_HD = new Channel("La7 HD", "http://tvblob.com/channel/LA7_HD", "LA7_HD");
     public final static Channel TVBLOB_LA7NEWSONDEMAND = new Channel("La7 News On Demand", "http://tvblob.com/channel/la7newsondemand", "la7newsondemand");
     public final static Channel TVBLOB_LA7ONDEMAND = new Channel("La7 On Demand", "http://tvblob.com/channel/la7ondemand", "la7ondemand");
     public final static Channel TVBLOB_LA7SERVIZIONDEMAND = new Channel("La7 Servizi On Demand", "http://tvblob.com/channel/la7serviziondemand", "la7serviziondemand");
     public final static Channel TVBLOB_LA7D = new Channel("La7D", "http://tvblob.com/channel/la7d", "la7d");
     public final static Channel TVBLOB_LA7DONDEMAND = new Channel("La7D On Demand", "http://tvblob.com/channel/la7dondemand", "la7dondemand");
     public final static Channel TVBLOB_LOMBARDIACHANNEL = new Channel("Lombardia Channel", "http://tvblob.com/channel/lombardiachannel", "lombardiachannel");
     public final static Channel TVBLOB_LOMBARDIADTT = new Channel("Lombardia DTT", "http://tvblob.com/channel/lombardiadtt", "lombardiadtt");
     public final static Channel TVBLOB_MEDIASHOPPING = new Channel("Media Shopping", "http://tvblob.com/channel/mediashopping", "mediashopping");
     public final static Channel TVBLOB_MEDIASET_EXTRA = new Channel("Mediaset Extra", "http://tvblob.com/channel/mediaset_extra", "mediaset_extra");
     public final static Channel TVBLOB_MIATV = new Channel("Mia TV", "http://tvblob.com/channel/miatv", "miatv");
     public final static Channel TVBLOB_MILANO2015 = new Channel("Milano 2015", "http://tvblob.com/channel/milano2015", "milano2015");
     public final static Channel TVBLOB_MILANOW = new Channel("MILANOW", "http://tvblob.com/channel/milanow", "milanow");
     public final static Channel TVBLOB_MOTORITV = new Channel("Motori TV", "http://tvblob.com/channel/motoritv", "motoritv");
     public final static Channel TVBLOB_MTV = new Channel("MTV", "http://tvblob.com/channel/mtv", "tvblobmtv");
     public final static Channel TVBLOB_MTV_HD = new Channel("MTV HD", "http://tvblob.com/channel/mtv_hd", "mtv_hd");
     public final static Channel TVBLOB_MTVMUSICONDEMAND = new Channel("MTV Music On Demand", "http://tvblob.com/channel/mtvmusicondemand", "mtvmusicondemand");
     public final static Channel TVBLOB_MTVNEWSONDEMAND = new Channel("MTV News On Demand", "http://tvblob.com/channel/mtvnewsondemand", "mtvnewsondemand");
     public final static Channel TVBLOB_MTVONDEMAND = new Channel("MTV On Demand", "http://tvblob.com/channel/mtvondemand", "mtvondemand");
     public final static Channel TVBLOB_MTVPLUS = new Channel("MTV+", "http://tvblob.com/channel/mtvplus", "mtvplus");
     public final static Channel TVBLOB_MTVPLUSONDEMAND = new Channel("MTV+ On Demand", "http://tvblob.com/channel/mtvplusondemand", "mtvplusondemand");
     public final static Channel TVBLOB_MYA = new Channel("Mya - Premium", "http://tvblob.com/channel/mya", "mya");
     public final static Channel TVBLOB_MYAPIUUNO = new Channel("Mya+1 - Premium", "http://tvblob.com/channel/myapiuuno", "myapiuuno");
     public final static Channel TVBLOB_NITEGATEATTIVAZIONE = new Channel("Nitegate Attivazione", "http://tvblob.com/channel/nitegateattivazione", "nitegateattivazione");
     public final static Channel TVBLOB_NOTTURNOITALIANO = new Channel("Notturno Italiano", "http://tvblob.com/channel/notturnoitaliano", "notturnoitaliano");
     public final static Channel TVBLOB_ODEON24 = new Channel("Odeon 24", "http://tvblob.com/channel/odeon24", "odeon24");
     public final static Channel TVBLOB_PALERMOCHANNEL = new Channel("Palermo Channel TV", "http://tvblob.com/channel/palermochannel", "palermochannel");
     public final static Channel TVBLOB_PIUBLULOMBARDIATELEMILANO = new Channel("Più Blu Lombardia TeleMilano", "http://tvblob.com/channel/piublulombardiatelemilano", "piublulombardiatelemilano");
     public final static Channel TVBLOB_PIUSERVIZI = new Channel("Più Servizi", "http://tvblob.com/channel/piuservizi", "piuservizi");
     public final static Channel TVBLOB_PLAYHOUSEDISNEY = new Channel("Playhouse Disney - Premium", "http://tvblob.com/channel/playhousedisney", "tvblobplayhousedisney");
     public final static Channel TVBLOB_PLAYME = new Channel("PlayMe", "http://tvblob.com/channel/playme", "playme");
     public final static Channel TVBLOB_POKERITALIA24 = new Channel("Poker Italia 24", "http://tvblob.com/channel/pokeritalia24", "pokeritalia24");
     public final static Channel TVBLOB_PORTALESERVIZITELECOM = new Channel("Portale Servizi Telecom", "http://tvblob.com/channel/portaleservizitelecom", "portaleservizitelecom");
     public final static Channel TVBLOB_PREMIUMCALCIO = new Channel("Premium Calcio", "http://tvblob.com/channel/premiumcalcio", "premiumcalcio");
     public final static Channel TVBLOB_PREMIUMCALCIO1 = new Channel("Premium Calcio 1", "http://tvblob.com/channel/premiumcalcio1", "premiumcalcio1");
     public final static Channel TVBLOB_PREMIUMCALCIO2 = new Channel("Premium Calcio 2", "http://tvblob.com/channel/premiumcalcio2", "premiumcalcio2");
     public final static Channel TVBLOB_PREMIUMCALCIO3 = new Channel("Premium Calcio 3", "http://tvblob.com/channel/premiumcalcio3", "premiumcalcio3");
     public final static Channel TVBLOB_PREMIUMCALCIO4 = new Channel("Premium Calcio 4", "http://tvblob.com/channel/premiumcalcio4", "premiumcalcio4");
     public final static Channel TVBLOB_PREMIUMCALCIO5 = new Channel("Premium Calcio 5", "http://tvblob.com/channel/premiumcalcio5", "premiumcalcio5");
     public final static Channel TVBLOB_PREMIUMCALCIO6 = new Channel("Premium Calcio 6", "http://tvblob.com/channel/premiumcalcio6", "premiumcalcio6");
     public final static Channel TVBLOB_PREMIUMCALCIOHD1 = new Channel("Premium Calcio HD 1", "http://tvblob.com/channel/premiumcalciohd1", "premiumcalciohd1");
     public final static Channel TVBLOB_PREMIUMCALCIOHD2 = new Channel("Premium Calcio HD 2", "http://tvblob.com/channel/premiumcalciohd2", "premiumcalciohd2");
     public final static Channel TVBLOB_PREMIUMCINEMA = new Channel("Premium Cinema", "http://tvblob.com/channel/premiumcinema", "premiumcinema");
     public final static Channel TVBLOB_PREMIUMCINEMAHD = new Channel("Premium Cinema HD", "http://tvblob.com/channel/premiumcinemahd", "premiumcinemahd");
     public final static Channel TVBLOB_PREMIUMEMOTION = new Channel("Premium Emotion", "http://tvblob.com/channel/premiumemotion", "premiumemotion");
     public final static Channel TVBLOB_PREMIUMENERGY = new Channel("Premium Energy", "http://tvblob.com/channel/premiumenergy", "premiumenergy");
     public final static Channel TVBLOB_PREMIUMEXTRA1 = new Channel("Premium Extra 1", "http://tvblob.com/channel/premiumextra1", "premiumextra1");
     public final static Channel TVBLOB_PREMIUMEXTRA2 = new Channel("Premium Extra 2", "http://tvblob.com/channel/premiumextra2", "premiumextra2");
     public final static Channel TVBLOB_PREMIUMMENU = new Channel("Premium Menu", "http://tvblob.com/channel/premiummenu", "premiummenu");
     public final static Channel TVBLOB_PREMIUMTEST = new Channel("Premium Test", "http://tvblob.com/channel/premiumtest", "premiumtest");
     public final static Channel TVBLOB_PRIMARETE = new Channel("Prima Rete", "http://tvblob.com/channel/primarete", "primarete");
     public final static Channel TVBLOB_PUNTOSAT = new Channel("PuntoSat", "http://tvblob.com/channel/puntosat", "puntosat");
     public final static Channel TVBLOB_QVC = new Channel("QVC", "http://tvblob.com/channel/qvc", "tvblobqvc");
     public final static Channel TVBLOB_RITALIASMI = new Channel("R Italia SMI", "http://tvblob.com/channel/ritaliasmi", "ritaliasmi");
     public final static Channel TVBLOB_RADIOALEX = new Channel("Radio Alex", "http://tvblob.com/channel/radioalex", "radioalex");
     public final static Channel TVBLOB_RADIOCAPITAL = new Channel("Radio Capital", "http://tvblob.com/channel/radiocapital", "radiocapital");
     public final static Channel TVBLOB_RADIOCAPRI = new Channel("Radio Capri", "http://tvblob.com/channel/radiocapri", "radiocapri");
     public final static Channel TVBLOB_RADIOCAPRITELEVISION = new Channel("Radio Capri TelevisiON", "http://tvblob.com/channel/radiocapritelevision", "radiocapritelevision");
     public final static Channel TVBLOB_RADIOCITY = new Channel("Radio City", "http://tvblob.com/channel/radiocity", "radiocity");
     public final static Channel TVBLOB_RADIODEEJAY = new Channel("Radio Deejay", "http://tvblob.com/channel/radiodeejay", "radiodeejay");
     public final static Channel TVBLOB_RADIODUE = new Channel("Radio Due", "http://tvblob.com/channel/radiodue", "radiodue");
     public final static Channel TVBLOB_RADIOM2O = new Channel("Radio m2o", "http://tvblob.com/channel/radiom2o", "radiom2o");
     public final static Channel TVBLOB_RADIOMARCONI = new Channel("Radio Marconi", "http://tvblob.com/channel/radiomarconi", "radiomarconi");
     public final static Channel TVBLOB_RADIOMARCONICLASSIC = new Channel("Radio Marconi Classic", "http://tvblob.com/channel/radiomarconiclassic", "radiomarconiclassic");
     public final static Channel TVBLOB_RADIOMARIA = new Channel("Radio Maria", "http://tvblob.com/channel/radiomaria", "radiomaria");
     public final static Channel TVBLOB_RADIOMATER = new Channel("Radio Mater", "http://tvblob.com/channel/radiomater", "radiomater");
     public final static Channel TVBLOB_RADIOMILANINTER = new Channel("Radio Milan Inter", "http://tvblob.com/channel/radiomilaninter", "radiomilaninter");
     public final static Channel TVBLOB_RADIOMILLENNIUM = new Channel("Radio Millennium", "http://tvblob.com/channel/radiomillennium", "radiomillennium");
     public final static Channel TVBLOB_RADIOSTAR = new Channel("Radio Star*", "http://tvblob.com/channel/radiostar", "radiostar");
     public final static Channel TVBLOB_RADIOTRE = new Channel("Radio Tre", "http://tvblob.com/channel/radiotre", "radiotre");
     public final static Channel TVBLOB_RADIOUNO = new Channel("Radio Uno", "http://tvblob.com/channel/radiouno", "radiouno");
     public final static Channel TVBLOB_RAIUNO = new Channel("Rai 1", "http://tvblob.com/channel/raiuno", "raiuno");
     public final static Channel TVBLOB_RAIDUE = new Channel("Rai 2", "http://tvblob.com/channel/raidue", "raidue");
     public final static Channel TVBLOB_RAITRE = new Channel("Rai 3", "http://tvblob.com/channel/raitre", "raitre");
     public final static Channel TVBLOB_RAI4 = new Channel("Rai 4", "http://tvblob.com/channel/rai4", "rai4");
     public final static Channel TVBLOB_RAI5 = new Channel("Rai 5", "http://tvblob.com/channel/rai5", "rai5");
     public final static Channel TVBLOB_RAISATEXTRA = new Channel("Rai Extra", "http://tvblob.com/channel/raisatextra", "raisatextra");
     public final static Channel TVBLOB_RAIGULP = new Channel("Rai Gulp", "http://tvblob.com/channel/raigulp", "raigulp");
     public final static Channel TVBLOB_RAIHD = new Channel("Rai HD", "http://tvblob.com/channel/raiHD", "raiHD");
     public final static Channel TVBLOB_RAI_MOVIE = new Channel("Rai Movie", "http://tvblob.com/channel/rai_movie", "rai_movie");
     public final static Channel TVBLOB_RAINEWS = new Channel("Rai News", "http://tvblob.com/channel/rainews", "rainews");
     public final static Channel TVBLOB_RAIPREMIUM = new Channel("Rai Premium", "http://tvblob.com/channel/raipremium", "raipremium");
     public final static Channel TVBLOB_RAISCUOLA = new Channel("Rai Scuola", "http://tvblob.com/channel/raiscuola", "raiscuola");
     public final static Channel TVBLOB_RAISPORTUNO = new Channel("Rai Sport 1", "http://tvblob.com/channel/raisportuno", "raisportuno");
     public final static Channel TVBLOB_RAISPORTDUE = new Channel("Rai Sport 2", "http://tvblob.com/channel/raisportdue", "raisportdue");
     public final static Channel TVBLOB_RAISTORIA = new Channel("Rai Storia", "http://tvblob.com/channel/raistoria", "raistoria");
     public final static Channel TVBLOB_RAIYOYO = new Channel("Rai Yoyo", "http://tvblob.com/channel/raiyoyo", "raiyoyo");
     public final static Channel TVBLOB_REALTIME = new Channel("Real Time", "http://tvblob.com/channel/realtime", "realtime");
     public final static Channel TVBLOB_REPUBBLICATV = new Channel("Repubblica TV", "http://tvblob.com/channel/repubblicatv", "repubblicatv");
     public final static Channel TVBLOB_RETE55 = new Channel("Rete 55", "http://tvblob.com/channel/rete55", "rete55");
     public final static Channel TVBLOB_RETECAPRI = new Channel("Rete Capri", "http://tvblob.com/channel/retecapri", "retecapri");
     public final static Channel TVBLOB_RETECAPRIPIUUNO = new Channel("Rete Capri +1", "http://tvblob.com/channel/retecapripiuuno", "retecapripiuuno");
     public final static Channel TVBLOB_RETE_4 = new Channel("Rete4", "http://tvblob.com/channel/rete_4", "rete_4");
     public final static Channel TVBLOB_RETE4_PIU1 = new Channel("Rete4 +1", "http://tvblob.com/channel/rete4_piu1", "rete4_piu1");
     public final static Channel TVBLOB_RETE4_HD = new Channel("Rete4 HD", "http://tvblob.com/channel/rete4_hd", "rete4_hd");
     public final static Channel TVBLOB_ROVI = new Channel("Rovi", "http://tvblob.com/channel/rovi", "rovi");
     public final static Channel TVBLOB_RSILA1 = new Channel("RSI LA 1", "http://tvblob.com/channel/rsila1", "rsila1");
     public final static Channel TVBLOB_RSILA2 = new Channel("RSI LA 2", "http://tvblob.com/channel/rsila2", "rsila2");
     public final static Channel TVBLOB_RTL1025 = new Channel("RTL 102.5", "http://tvblob.com/channel/rtl1025", "rtl1025");
     public final static Channel TVBLOB_SALUTEENATURA = new Channel("Salute e Natura", "http://tvblob.com/channel/saluteenatura", "saluteenatura");
     public final static Channel TVBLOB_SEXOAMATORIAL = new Channel("Sexo Amatorial", "http://tvblob.com/channel/sexoamatorial", "sexoamatorial");
     public final static Channel TVBLOB_SEXOEXCLUSIVE = new Channel("Sexo Exclusive", "http://tvblob.com/channel/sexoexclusive", "sexoexclusive");
     public final static Channel TVBLOB_SEXOEXOTICA = new Channel("Sexo Exotica", "http://tvblob.com/channel/sexoexotica", "sexoexotica");
     public final static Channel TVBLOB_SEXOTRANSGRESSION = new Channel("Sexo Transgression", "http://tvblob.com/channel/sexotransgression", "sexotransgression");
     public final static Channel TVBLOB_SF1 = new Channel("SF 1", "http://tvblob.com/channel/sf1", "sf1");
     public final static Channel TVBLOB_SMILETV = new Channel("Smile TV", "http://tvblob.com/channel/smiletv", "smiletv");
     public final static Channel TVBLOB_SPORTITALIA = new Channel("Sport Italia", "http://tvblob.com/channel/sportitalia", "sportitalia");
     public final static Channel TVBLOB_SPORTITALIA2 = new Channel("Sport Italia 2", "http://tvblob.com/channel/sportitalia2", "sportitalia2");
     public final static Channel TVBLOB_SPORTITALIA24 = new Channel("Sport Italia 24", "http://tvblob.com/channel/sportitalia24", "sportitalia24");
     public final static Channel TVBLOB_SRGSWISSPOP = new Channel("SRG - Swiss Pop", "http://tvblob.com/channel/srgswisspop", "srgswisspop");
     public final static Channel TVBLOB_STEEL = new Channel("Steel - Premium", "http://tvblob.com/channel/steel", "steel");
     public final static Channel TVBLOB_STEELPIUUNO = new Channel("Steel+1 - Premium", "http://tvblob.com/channel/steelpiuuno", "steelpiuuno");
     public final static Channel TVBLOB_STUDIO1 = new Channel("Studio 1", "http://tvblob.com/channel/studio1", "studio1");
     public final static Channel TVBLOB_STUDIO1HD = new Channel("Studio 1 HD", "http://tvblob.com/channel/studio1hd", "studio1hd");
     public final static Channel TVBLOB_STUDIO1TEST = new Channel("Studio 1 Test", "http://tvblob.com/channel/studio1test", "studio1test");
     public final static Channel TVBLOB_STUDIO7 = new Channel("Studio 7", "http://tvblob.com/channel/studio7", "studio7");
     public final static Channel TVBLOB_STUDIOSTORE = new Channel("Studio Store", "http://tvblob.com/channel/studiostore", "studiostore");
     public final static Channel TVBLOB_STUDIOUNIVERSAL = new Channel("Studio Universal - Premium", "http://tvblob.com/channel/studiouniversal", "studiouniversal");
     public final static Channel TVBLOB_STUDIONORDTV = new Channel("Studionord TV", "http://tvblob.com/channel/studionordtv", "studionordtv");
     public final static Channel TVBLOB_SUPERTENNIS = new Channel("Super tennis", "http://tvblob.com/channel/supertennis", "supertennis");
     public final static Channel TVBLOB_SUPERTV = new Channel("Super TV", "http://tvblob.com/channel/supertv", "supertv");
     public final static Channel TVBLOB_TBNE = new Channel("TBNE", "http://tvblob.com/channel/tbne", "tbne");
     public final static Channel TVBLOB_TELETV = new Channel("Tele TV", "http://tvblob.com/channel/teletv", "teletv");
     public final static Channel TVBLOB_TELECITY2MI = new Channel("Telecity 2 MI", "http://tvblob.com/channel/telecity2mi", "telecity2mi");
     public final static Channel TVBLOB_TELECITY7GOLD = new Channel("Telecity 7 Gold", "http://tvblob.com/channel/telecity7gold", "telecity7gold");
     public final static Channel TVBLOB_TELECITY7GOLDLOMBARDIA = new Channel("Telecity 7 Gold Lombardia", "http://tvblob.com/channel/telecity7goldlombardia", "telecity7goldlombardia");
     public final static Channel TVBLOB_TELECOLOR = new Channel("Telecolor", "http://tvblob.com/channel/telecolor", "telecolor");
     public final static Channel TVBLOB_TELELOMBARDIA = new Channel("Telelombardia", "http://tvblob.com/channel/telelombardia", "telelombardia");
     public final static Channel TVBLOB_TELEMARKET = new Channel("Telemarket", "http://tvblob.com/channel/telemarket", "telemarket");
     public final static Channel TVBLOB_TELEMARKET2 = new Channel("Telemarket 2", "http://tvblob.com/channel/telemarket2", "telemarket2");
     public final static Channel TVBLOB_TELEMILANOCITYPIUBLU = new Channel("TeleMilano City Più Blu", "http://tvblob.com/channel/telemilanocitypiublu", "telemilanocitypiublu");
     public final static Channel TVBLOB_TELENOVA = new Channel("Telenova", "http://tvblob.com/channel/telenova", "telenova");
     public final static Channel TVBLOB_TELENOVAPIU1 = new Channel("Telenova +1", "http://tvblob.com/channel/telenovapiu1", "telenovapiu1");
     public final static Channel TVBLOB_TELENOVA2 = new Channel("Telenova 2", "http://tvblob.com/channel/telenova2", "telenova2");
     public final static Channel TVBLOB_TELENOVA3 = new Channel("Telenova 3 Sport Action", "http://tvblob.com/channel/telenova3", "telenova3");
     public final static Channel TVBLOB_TELEPACE = new Channel("Telepace", "http://tvblob.com/channel/telepace", "telepace");
     public final static Channel TVBLOB_TELEREPORTER = new Channel("TeleReporter", "http://tvblob.com/channel/telereporter", "telereporter");
     public final static Channel TVBLOB_TELESTAR = new Channel("Telestar", "http://tvblob.com/channel/telestar", "telestar");
     public final static Channel TVBLOB_TELESTARMIPIUUNO = new Channel("Telestar +1 MI", "http://tvblob.com/channel/telestarmipiuuno", "telestarmipiuuno");
     public final static Channel TVBLOB_TELESTARMI = new Channel("Telestar MI", "http://tvblob.com/channel/telestarmi", "telestarmi");
     public final static Channel TVBLOB_TGMEDIASET = new Channel("TG Mediaset", "http://tvblob.com/channel/tgmediaset", "tgmediaset");
     public final static Channel TVBLOB_TGNORBA24 = new Channel("TG Norba 24", "http://tvblob.com/channel/tgnorba24", "tgnorba24");
     public final static Channel TVBLOB_TIVUITALIATEST4 = new Channel("Tivuitalia test 4", "http://tvblob.com/channel/tivuitaliatest4", "tivuitaliatest4");
     public final static Channel TVBLOB_TIVUITALIATEST5 = new Channel("Tivuitalia test 5", "http://tvblob.com/channel/tivuitaliatest5", "tivuitaliatest5");
     public final static Channel TVBLOB_TIVUITALIATEST6 = new Channel("Tivuitalia test 6", "http://tvblob.com/channel/tivuitaliatest6", "tivuitaliatest6");
     public final static Channel TVBLOB_TIVUITALIATEST7 = new Channel("Tivuitalia test 7", "http://tvblob.com/channel/tivuitaliatest7", "tivuitaliatest7");
     public final static Channel TVBLOB_TIVUITALIATEST8 = new Channel("Tivuitalia test 8", "http://tvblob.com/channel/tivuitaliatest8", "tivuitaliatest8");
     public final static Channel TVBLOB_TELECAMPIONE = new Channel("TLC Telecampione", "http://tvblob.com/channel/telecampione", "telecampione");
     public final static Channel TVBLOB_TOPCALCIO24 = new Channel("Top Calcio 24", "http://tvblob.com/channel/topcalcio24", "topcalcio24");
     public final static Channel TVBLOB_TOPMUSIC = new Channel("Top Music", "http://tvblob.com/channel/topmusic", "topmusic");
     public final static Channel TVBLOB_TOPTECH = new Channel("Top Tech", "http://tvblob.com/channel/toptech", "toptech");
     public final static Channel TVBLOB_TRS = new Channel("TRS", "http://tvblob.com/channel/trs", "trs");
     public final static Channel TVBLOB_TRSTV1 = new Channel("TRS TV 1", "http://tvblob.com/channel/trstv1", "trstv1");
     public final static Channel TVBLOB_TRSTV2 = new Channel("TRS TV 2", "http://tvblob.com/channel/trstv2", "trstv2");
     public final static Channel TVBLOB_TRSTVSERVIZIO = new Channel("TRS TV Servizio", "http://tvblob.com/channel/trstvservizio", "trstvservizio");
     public final static Channel TVBLOB_TRSTVTEST1 = new Channel("TRS TV test1", "http://tvblob.com/channel/trstvtest1", "trstvtest1");
     public final static Channel TVBLOB_TRSTVTEST2 = new Channel("TRS TV test2", "http://tvblob.com/channel/trstvtest2", "trstvtest2");
     public final static Channel TVBLOB_TSR1 = new Channel("TSR1", "http://tvblob.com/channel/tsr1", "tsr1");
     public final static Channel TVBLOB_TV2000 = new Channel("Tv 2000", "http://tvblob.com/channel/tv2000", "tv2000");
     public final static Channel TVBLOB_VERTIGOBLACK = new Channel("Vertigo Black", "http://tvblob.com/channel/vertigoblack", "vertigoblack");
     public final static Channel TVBLOB_VERTIGOTVIT = new Channel("VERTIGOTV.IT VM18", "http://tvblob.com/channel/vertigotvit", "vertigotvit");
     public final static Channel TVBLOB_VIAGGIANDOTV = new Channel("Viaggiando TV", "http://tvblob.com/channel/viaggiandotv", "viaggiandotv");
     public final static Channel TVBLOB_VIDEOSTAR = new Channel("Videostar", "http://tvblob.com/channel/videostar", "videostar");
     public final static Channel TVBLOB_VIRGINRADIOTV = new Channel("Virginradio TV", "http://tvblob.com/channel/virginradiotv", "virginradiotv");
 
     public final static Channel ONEHUNDREDPOINTFOUR_SMOOTH_RADIO = new Channel("100.4 Smooth Radio", CHANNEL_URI_PREFIX+"b", "b");
     public final static Channel ONEHUNDREDANDTWO_CAPITAL_FM = new Channel("102 Capital FM", CHANNEL_URI_PREFIX+"c", "c");
     public final static Channel ONEHUNDREDANDTWO_TOWN_FM = new Channel("102 Town FM", CHANNEL_URI_PREFIX+"d", "d");
     public final static Channel ONEHUNDREDANDTWOPOINTTWO_CAPITAL_FM = new Channel("102.2 Capital FM", CHANNEL_URI_PREFIX+"f", "f");
     public final static Channel ONEHUNDREDANDTWOPOINTTWO_SMOOTH_RADIO = new Channel("102.2 Smooth Radio", CHANNEL_URI_PREFIX+"g", "g");
     public final static Channel ONEHUNDREDANDTHREE_THE_EYE = new Channel("103 The Eye", CHANNEL_URI_PREFIX+"h", "h");
     public final static Channel ONEHUNDREDANDTHREE_POINT_TWO_CAPITAL_FM = new Channel("103.2 Capital FM", CHANNEL_URI_PREFIX+"j", "j");
     public final static Channel ONEHUNDREDANDFIVE_CAPITAL_FM = new Channel("105 Capital FM", CHANNEL_URI_PREFIX+"k", "k");
     public final static Channel ONEHUNDREDANDFIVE_TO_ONEHUNDREDANDSIX_CAPITAL_FM_SCOTLAND = new Channel("105-106 Capital FM Scotland", CHANNEL_URI_PREFIX+"m", "m");
     public final static Channel ONEHUNDREDANDFIVEPOINTTWO_SMOOTH_RADIO = new Channel("105.2 Smooth Radio", CHANNEL_URI_PREFIX+"n", "n");
     public final static Channel ONEHUNDREDANDFIVEPOINTSEVEN_SMOOTH_RADIO = new Channel("105.7 Smooth Radio", CHANNEL_URI_PREFIX+"p", "p");
     public final static Channel ONEHUNDREDANDFIVEORSIX_CAPITAL_FM = new Channel("105/106 Capital FM", CHANNEL_URI_PREFIX+"q", "q");
     public final static Channel ONEHUNDREDANDSIXPOINTONE_ROCK_RADIO = new Channel("106.1 Rock Radio", CHANNEL_URI_PREFIX+"r", "r");
     public final static Channel ONEHUNDREDANDSIXPOINTSIX_SMOOTH_RADIO = new Channel("106.6 Smooth Radio", CHANNEL_URI_PREFIX+"s", "s");
     public final static Channel ONEHUNDREDANDSEVENPOINTFOUR_THE_SEVERN = new Channel("107.4 The Severn", CHANNEL_URI_PREFIX+"t", "t");
     public final static Channel ONEHUNDREDANDSEVENPOINTSIX_JUICE_FM = new Channel("107.6 Juice FM", CHANNEL_URI_PREFIX+"v", "v");
     public final static Channel TWO_FM = new Channel("2 FM", CHANNEL_URI_PREFIX+"w", "w");
     public final static Channel TWOBR = new Channel("2BR", CHANNEL_URI_PREFIX+"x", "x");
     public final static Channel FOURFM = new Channel("4FM", CHANNEL_URI_PREFIX+"y", "y");
     public final static Channel NINETYSIXTOONEHUNDREDANDSIX_CAPITAL_FM = new Channel("96-106 Capital FM", CHANNEL_URI_PREFIX+"z", "z");
     public final static Channel NINETYSIXPOINTTHREE_ROCK_RADIO = new Channel("96.3 Rock Radio", CHANNEL_URI_PREFIX+"4", "4");
     public final static Channel NINETYSIXPOINTFIVE_RADIO_WAVE = new Channel("96.5 Radio Wave", CHANNEL_URI_PREFIX+"5", "5");
     public final static Channel NINETYSEVENPOINTFOUR_AND_103_2_CAPITAL_FM = new Channel("97.4/103.2 Capital FM", CHANNEL_URI_PREFIX+"6", "6");
     public final static Channel NINETYSEVENPOINTFIVE_KEMET_FM = new Channel("97.5 Kemet FM", CHANNEL_URI_PREFIX+"7", "7");
     public final static Channel NINETYSEVENPOINTFIVE_SMOOTH_RADIO = new Channel("97.5 Smooth Radio", CHANNEL_URI_PREFIX+"8", "8");
     public final static Channel NINETYEIGHTPOINTTHREE_FM_CORK_CAMPUS_RADIO = new Channel("98.3FM Cork Campus Radio", CHANNEL_URI_PREFIX+"9", "9");
     public final static Channel NINETYEIGHTFM = new Channel("98FM", CHANNEL_URI_PREFIX+"cb", "cb");
     public final static Channel ABSOLUTE_XTREME = new Channel("Absolute Xtreme", CHANNEL_URI_PREFIX+"cd", "cd");
     public final static Channel AMAZING_RADIO = new Channel("Amazing Radio", CHANNEL_URI_PREFIX+"cf", "cf");
     public final static Channel ARROW = new Channel("Arrow", CHANNEL_URI_PREFIX+"cg", "cg");
     public final static Channel ARROW_FM = new Channel("Arrow FM", CHANNEL_URI_PREFIX+"ch", "ch");
     public final static Channel BAY = new Channel("Bay", CHANNEL_URI_PREFIX+"cj", "cj");
     public final static Channel BCB_106_6FM = new Channel("BCB 106.6FM", CHANNEL_URI_PREFIX+"cp", "cp");
     public final static Channel BEACH_103_4FM = new Channel("Beach 103.4FM", CHANNEL_URI_PREFIX+"cq", "cq");
     public final static Channel BEACON = new Channel("Beacon", CHANNEL_URI_PREFIX+"cr", "cr");
     public final static Channel BLACK_DIAMOND = new Channel("Black Diamond", CHANNEL_URI_PREFIX+"cs", "cs");
     public final static Channel BLAST_FM = new Channel("Blast FM", CHANNEL_URI_PREFIX+"ct", "ct");
     public final static Channel BORDERS_RADIO = new Channel("Borders Radio", CHANNEL_URI_PREFIX+"cv", "cv");
     public final static Channel BRIGHT_106_4 = new Channel("Bright 106.4", CHANNEL_URI_PREFIX+"cw", "cw");
     public final static Channel BRMB_96_4 = new Channel("BRMB 96.4", CHANNEL_URI_PREFIX+"cx", "cx");
     public final static Channel BUZZ_ASIA = new Channel("Buzz Asia", CHANNEL_URI_PREFIX+"cy", "cy");
     public final static Channel C103_NORTH_AND_EAST_CORK = new Channel("C103 (North and East Cork)", CHANNEL_URI_PREFIX+"cz", "cz");
     public final static Channel C103_WEST_CORK = new Channel("C103 (West Cork)", CHANNEL_URI_PREFIX+"c2", "c2");
     public final static Channel CENTRAL_FM = new Channel("Central FM", CHANNEL_URI_PREFIX+"c5", "c5");
     public final static Channel CEREDIGION = new Channel("Ceredigion", CHANNEL_URI_PREFIX+"c6", "c6");
     public final static Channel CFM = new Channel("CFM", CHANNEL_URI_PREFIX+"c7", "c7");
     public final static Channel CHANNEL_103 = new Channel("Channel 103", CHANNEL_URI_PREFIX+"c8", "c8");
     public final static Channel CHELMSFORD_RADIO = new Channel("Chelmsford Radio", CHANNEL_URI_PREFIX+"c9", "c9");
     public final static Channel CHILL = new Channel("Chill", CHANNEL_URI_PREFIX+"db", "db");
     public final static Channel CHOICE = new Channel("Choice", CHANNEL_URI_PREFIX+"dc", "dc");
     public final static Channel CHOICE_FM = new Channel("Choice FM", CHANNEL_URI_PREFIX+"dd", "dd");
     public final static Channel CITY_BEAT_96_7_FM = new Channel("City Beat 96.7 FM", CHANNEL_URI_PREFIX+"df", "df");
     public final static Channel CITY_TALK_105_9 = new Channel("City Talk 105.9", CHANNEL_URI_PREFIX+"dg", "dg");
     public final static Channel CLARE_FM = new Channel("Clare FM", CHANNEL_URI_PREFIX+"dh", "dh");
     public final static Channel CLYDE_1 = new Channel("Clyde 1", CHANNEL_URI_PREFIX+"dj", "dj");
     public final static Channel CLYDE_2 = new Channel("Clyde 2", CHANNEL_URI_PREFIX+"dk", "dk");
     public final static Channel COAST_106 = new Channel("Coast 106", CHANNEL_URI_PREFIX+"dm", "dm");
     public final static Channel COASTLINE_RADIO = new Channel("Coastline Radio", CHANNEL_URI_PREFIX+"dn", "dn");
     public final static Channel COMPASS_FM = new Channel("Compass FM", CHANNEL_URI_PREFIX+"dp", "dp");
     public final static Channel CONNECT_FM = new Channel("Connect FM", CHANNEL_URI_PREFIX+"dq", "dq");
     public final static Channel CONNECT_FM_106_8 = new Channel("Connect FM 106.8", CHANNEL_URI_PREFIX+"dr", "dr");
     public final static Channel COOL_FM = new Channel("Cool FM", CHANNEL_URI_PREFIX+"ds", "ds");
     public final static Channel CORKS_96FM = new Channel("Cork's 96FM", CHANNEL_URI_PREFIX+"dt", "dt");
     public final static Channel COUNTY_SOUND_RADIO = new Channel("County Sound Radio", CHANNEL_URI_PREFIX+"dv", "dv");
     public final static Channel COVENTRY_AND_WARWICKSHIRE_FM = new Channel("Coventry and Warwickshire FM", CHANNEL_URI_PREFIX+"dw", "dw");
     public final static Channel CUH_FM_107_8 = new Channel("CUH FM 107.8", CHANNEL_URI_PREFIX+"dx", "dx");
     public final static Channel DERBYS_RAM_FM = new Channel("Derby's Ram FM", CHANNEL_URI_PREFIX+"dy", "dy");
     public final static Channel DEVON_MW = new Channel("Devon MW", CHANNEL_URI_PREFIX+"dz", "dz");
     public final static Channel DOWNTOWN_RADIO = new Channel("Downtown Radio", CHANNEL_URI_PREFIX+"d2", "d2");
     public final static Channel DREAM_100 = new Channel("Dream 100", CHANNEL_URI_PREFIX+"d4", "d4");
     public final static Channel DUBLIN_CITY_FM = new Channel("Dublin City FM", CHANNEL_URI_PREFIX+"d5", "d5");
     public final static Channel DUNE_FM = new Channel("Dune FM", CHANNEL_URI_PREFIX+"d6", "d6");
     public final static Channel EAGLE_96_4 = new Channel("Eagle 96.4", CHANNEL_URI_PREFIX+"d7", "d7");
     public final static Channel EAST_COAST_RADIO = new Channel("East Coast Radio", CHANNEL_URI_PREFIX+"d8", "d8");
     public final static Channel FIRE = new Channel("Fire", CHANNEL_URI_PREFIX+"d9", "d9");
     public final static Channel FM_104 = new Channel("FM 104", CHANNEL_URI_PREFIX+"fb", "fb");
     public final static Channel FOREST_FM = new Channel("Forest FM", CHANNEL_URI_PREFIX+"fc", "fc");
     public final static Channel FORTH_2 = new Channel("Forth 2", CHANNEL_URI_PREFIX+"fd", "fd");
     public final static Channel FORTH_ONE = new Channel("Forth One", CHANNEL_URI_PREFIX+"ff", "ff");
     public final static Channel FRESH_AM = new Channel("Fresh AM", CHANNEL_URI_PREFIX+"fg", "fg");
     public final static Channel FUN_KIDS = new Channel("Fun Kids", CHANNEL_URI_PREFIX+"fh", "fh");
     public final static Channel FUTURE_RADIO = new Channel("future radio", CHANNEL_URI_PREFIX+"fj", "fj");
     public final static Channel GALWAY_BAY_FM_95_8 = new Channel("Galway Bay FM 95.8", CHANNEL_URI_PREFIX+"fk", "fk");
     public final static Channel GEM_106 = new Channel("Gem 106", CHANNEL_URI_PREFIX+"fm", "fm");
     public final static Channel GLIDE_FM = new Channel("Glide FM", CHANNEL_URI_PREFIX+"fn", "fn");
     public final static Channel GOLD_RADIO = new Channel("Gold", CHANNEL_URI_PREFIX+"fp", "fp");
     public final static Channel HALLAM_FM = new Channel("Hallam FM", CHANNEL_URI_PREFIX+"fq", "fq");
     public final static Channel HEART_100_5_101_9 = new Channel("Heart 100.5/101.9", CHANNEL_URI_PREFIX+"fr", "fr");
     public final static Channel HEART_102_3 = new Channel("Heart 102.3", CHANNEL_URI_PREFIX+"fs", "fs");
     public final static Channel HEART_102_4 = new Channel("Heart 102.4", CHANNEL_URI_PREFIX+"ft", "ft");
     public final static Channel HEART_102_6_97_4 = new Channel("Heart 102.6/97.4", CHANNEL_URI_PREFIX+"fv", "fv");
     public final static Channel HEART_102_7 = new Channel("Heart 102.7", CHANNEL_URI_PREFIX+"fw", "fw");
     public final static Channel HEART_103 = new Channel("Heart 103", CHANNEL_URI_PREFIX+"fx", "fx");
     public final static Channel HEART_103_4 = new Channel("Heart 103.4", CHANNEL_URI_PREFIX+"fy", "fy");
     public final static Channel HEART_103_97_4 = new Channel("Heart 103/97.4", CHANNEL_URI_PREFIX+"fz", "fz");
     public final static Channel HEART_96_1 = new Channel("Heart 96.1", CHANNEL_URI_PREFIX+"f2", "f2");
     public final static Channel HEART_96_2_97_3 = new Channel("Heart 96.2/97.3", CHANNEL_URI_PREFIX+"f4", "f4");
     public final static Channel HEART_96_3 = new Channel("Heart 96.3", CHANNEL_URI_PREFIX+"f5", "f5");
     public final static Channel HEART_96_3_102_6 = new Channel("Heart 96.3/102.6", CHANNEL_URI_PREFIX+"f7", "f7");
     public final static Channel HEART_96_4FM = new Channel("Heart 96.4FM", CHANNEL_URI_PREFIX+"f8", "f8");
     public final static Channel HEART_96_5_97_1_102_6 = new Channel("Heart 96.5/97.1/102.6", CHANNEL_URI_PREFIX+"f9", "f9");
     public final static Channel HEART_96_6_97FM = new Channel("Heart 96.6/97FM", CHANNEL_URI_PREFIX+"gb", "gb");
     public final static Channel HEART_96_7_97_5 = new Channel("Heart 96.7/97.5", CHANNEL_URI_PREFIX+"gc", "gc");
     public final static Channel HEART_97_1 = new Channel("Heart 97.1", CHANNEL_URI_PREFIX+"gd", "gd");
     public final static Channel HEART_97_1_96_4 = new Channel("Heart 97.1/96.4", CHANNEL_URI_PREFIX+"gf", "gf");
     public final static Channel HEART_97_103FM = new Channel("Heart 97/103FM", CHANNEL_URI_PREFIX+"gg", "gg");
     public final static Channel HEART_ANGLESEY_AND_GWYNEDD = new Channel("Heart Anglesey and Gwynedd", CHANNEL_URI_PREFIX+"gh", "gh");
     public final static Channel HEART_BERKSHIRE_AND_NORTH_HAMPSHIRE = new Channel("Heart Berkshire and North Hampshire", CHANNEL_URI_PREFIX+"gj", "gj");
     public final static Channel HEART_GLOUCESTERSHIRE = new Channel("Heart Gloucestershire", CHANNEL_URI_PREFIX+"gk", "gk");
     public final static Channel HEART_HOME_COUNTIES = new Channel("Heart Home Counties", CHANNEL_URI_PREFIX+"gm", "gm");
     public final static Channel HEART_KENT = new Channel("Heart Kent", CHANNEL_URI_PREFIX+"gn", "gn");
     public final static Channel HEART_SUSSEX = new Channel("Heart Sussex", CHANNEL_URI_PREFIX+"gp", "gp");
     public final static Channel HEART_WEST_MIDLANDS = new Channel("Heart West Midlands", CHANNEL_URI_PREFIX+"gr", "gr");
     public final static Channel HEART_WILTSHIRE = new Channel("Heart Wiltshire", CHANNEL_URI_PREFIX+"gs", "gs");
     public final static Channel HEARTLAND = new Channel("Heartland", CHANNEL_URI_PREFIX+"gt", "gt");
     public final static Channel HOPE_FM = new Channel("Hope FM", CHANNEL_URI_PREFIX+"gw", "gw");
     public final static Channel IMAGINE_FM = new Channel("Imagine FM", CHANNEL_URI_PREFIX+"gx", "gx");
     public final static Channel IPSWICH_COMMUNITY_RADIO = new Channel("Ipswich Community Radio", CHANNEL_URI_PREFIX+"gy", "gy");
     public final static Channel ISLAND_FM = new Channel("Island FM", CHANNEL_URI_PREFIX+"gz", "gz");
     public final static Channel ISLE_OF_WIGHT_RADIO = new Channel("Isle of Wight Radio", CHANNEL_URI_PREFIX+"g2", "g2");
     public final static Channel ISLES_FM = new Channel("Isles FM", CHANNEL_URI_PREFIX+"g4", "g4");
     public final static Channel JACK_FM = new Channel("Jack FM", CHANNEL_URI_PREFIX+"g5", "g5");
     public final static Channel JAZZ_FM = new Channel("Jazz FM", CHANNEL_URI_PREFIX+"g6", "g6");
     public final static Channel JUICE_107_2 = new Channel("Juice 107.2", CHANNEL_URI_PREFIX+"g7", "g7");
     public final static Channel KCFM = new Channel("KCFM", CHANNEL_URI_PREFIX+"g8", "g8");
     public final static Channel KEY_103 = new Channel("Key 103", CHANNEL_URI_PREFIX+"g9", "g9");
     public final static Channel KFM = new Channel("KFM", CHANNEL_URI_PREFIX+"hb", "hb");
     public final static Channel KINGDOM_FM = new Channel("Kingdom FM", CHANNEL_URI_PREFIX+"hc", "hc");
     public final static Channel KINGSTOWN = new Channel("Kingstown", CHANNEL_URI_PREFIX+"hd", "hd");
     public final static Channel KISS_101 = new Channel("Kiss 101", CHANNEL_URI_PREFIX+"hf", "hf");
     public final static Channel KISS_105_108 = new Channel("Kiss 105-108", CHANNEL_URI_PREFIX+"hg", "hg");
     public final static Channel KLFM = new Channel("KLFM", CHANNEL_URI_PREFIX+"hh", "hh");
     public final static Channel KMFM_105_6 = new Channel("KMFM 105.6", CHANNEL_URI_PREFIX+"hj", "hj");
     public final static Channel KMFM_106 = new Channel("KMFM 106", CHANNEL_URI_PREFIX+"hk", "hk");
     public final static Channel KMFM_107_2 = new Channel("KMFM 107.2", CHANNEL_URI_PREFIX+"hm", "hm");
     public final static Channel KMFM_107_6 = new Channel("KMFM 107.6", CHANNEL_URI_PREFIX+"hn", "hn");
     public final static Channel KMFM_107_9_100_4 = new Channel("KMFM 107.9/100.4", CHANNEL_URI_PREFIX+"hp", "hp");
     public final static Channel KMFM_96_2_101_6 = new Channel("KMFM 96.2/101.6", CHANNEL_URI_PREFIX+"hq", "hq");
     public final static Channel KMFM_96_4_106_8 = new Channel("KMFM 96.4/106.8", CHANNEL_URI_PREFIX+"hr", "hr");
     public final static Channel LBC_97_3 = new Channel("LBC 97.3", CHANNEL_URI_PREFIX+"hs", "hs");
     public final static Channel LBC_NEWS_1152 = new Channel("LBC News 1152", CHANNEL_URI_PREFIX+"ht", "ht");
     public final static Channel LEITH = new Channel("Leith", CHANNEL_URI_PREFIX+"hv", "hv");
     public final static Channel LIFE_FM_93_1 = new Channel("Life FM 93.1", CHANNEL_URI_PREFIX+"hw", "hw");
     public final static Channel LINCS_FM = new Channel("Lincs FM", CHANNEL_URI_PREFIX+"hx", "hx");
     public final static Channel LIVE_95 = new Channel("Live 95", CHANNEL_URI_PREFIX+"hy", "hy");
     public final static Channel LM_FM_RADIO = new Channel("LM FM Radio", CHANNEL_URI_PREFIX+"hz", "hz");
     public final static Channel LOCHBROOM_FM = new Channel("Lochbroom FM", CHANNEL_URI_PREFIX+"h2", "h2");
     public final static Channel MAGIC_105_4 = new Channel("Magic 105.4", CHANNEL_URI_PREFIX+"h4", "h4");
     public final static Channel MAGIC_1152_MANCHESTER = new Channel("Magic 1152 (Manchester)", CHANNEL_URI_PREFIX+"h5", "h5");
     public final static Channel MAGIC_1152_AM = new Channel("Magic 1152 AM", CHANNEL_URI_PREFIX+"h6", "h6");
     public final static Channel MAGIC_1170 = new Channel("Magic 1170", CHANNEL_URI_PREFIX+"h7", "h7");
     public final static Channel MAGIC_1548 = new Channel("Magic 1548", CHANNEL_URI_PREFIX+"h8", "h8");
     public final static Channel MAGIC_828 = new Channel("Magic 828", CHANNEL_URI_PREFIX+"h9", "h9");
     public final static Channel MAGIC_999 = new Channel("Magic 999", CHANNEL_URI_PREFIX+"jb", "jb");
     public final static Channel MAGIC_AM = new Channel("Magic AM", CHANNEL_URI_PREFIX+"jc", "jc");
     public final static Channel MAGIC_DIGITAL = new Channel("Magic Digital", CHANNEL_URI_PREFIX+"jd", "jd");
     public final static Channel MANSFIELD_103_2 = new Channel("Mansfield 103.2", CHANNEL_URI_PREFIX+"jf", "jf");
     public final static Channel MERCIA_FM = new Channel("Mercia FM", CHANNEL_URI_PREFIX+"jg", "jg");
     public final static Channel METRO = new Channel("Metro", CHANNEL_URI_PREFIX+"jh", "jh");
     public final static Channel MIDWEST_RADIO = new Channel("Midwest Radio", CHANNEL_URI_PREFIX+"jj", "jj");
     public final static Channel MINSTER_FM = new Channel("Minster FM", CHANNEL_URI_PREFIX+"jk", "jk");
     public final static Channel MORAY_FIRTH_RADIO_FM = new Channel("Moray Firth Radio FM", CHANNEL_URI_PREFIX+"jm", "jm");
     public final static Channel MORAY_FIRTH_RADIO_MW = new Channel("Moray Firth Radio MW", CHANNEL_URI_PREFIX+"jn", "jn");
     public final static Channel NECR_102_1FM = new Channel("NECR 102.1FM", CHANNEL_URI_PREFIX+"jp", "jp");
     public final static Channel NENE_VALLEY = new Channel("Nene Valley", CHANNEL_URI_PREFIX+"jq", "jq");
     public final static Channel NEVIS_RADIO = new Channel("Nevis Radio", CHANNEL_URI_PREFIX+"jr", "jr");
     public final static Channel NEWPORT_CITY_RADIO = new Channel("Newport City Radio", CHANNEL_URI_PREFIX+"js", "js");
     public final static Channel NEWSTALK_106_108_FM = new Channel("Newstalk 106-108 FM", CHANNEL_URI_PREFIX+"jt", "jt");
     public final static Channel NME_RADIO = new Channel("NME Radio", CHANNEL_URI_PREFIX+"jv", "jv");
     public final static Channel NORTH_NORFOLK_RADIO = new Channel("North Norfolk Radio", CHANNEL_URI_PREFIX+"jw", "jw");
     public final static Channel NORTHSOUND_ONE = new Channel("Northsound One", CHANNEL_URI_PREFIX+"jx", "jx");
     public final static Channel NORTHSOUND_TWO = new Channel("Northsound Two", CHANNEL_URI_PREFIX+"jy", "jy");
     public final static Channel ONDA_CERO_MARBELLA = new Channel("Onda Cero Marbella", CHANNEL_URI_PREFIX+"jz", "jz");
     public final static Channel PEAK_107FM = new Channel("Peak 107FM", CHANNEL_URI_PREFIX+"j2", "j2");
     public final static Channel PIRATE_FM = new Channel("Pirate FM", CHANNEL_URI_PREFIX+"j4", "j4");
     public final static Channel PLANET_ROCK = new Channel("Planet Rock", CHANNEL_URI_PREFIX+"j5", "j5");
     public final static Channel PREMIER_CHRISTIAN_RADIO = new Channel("Premier Christian Radio", CHANNEL_URI_PREFIX+"j6", "j6");
     public final static Channel PULSE = new Channel("Pulse", CHANNEL_URI_PREFIX+"j7", "j7");
     public final static Channel PULSE_2 = new Channel("Pulse 2", CHANNEL_URI_PREFIX+"j8", "j8");
     public final static Channel Q102 = new Channel("Q102", CHANNEL_URI_PREFIX+"j9", "j9");
     public final static Channel Q102_9 = new Channel("Q102.9", CHANNEL_URI_PREFIX+"kb", "kb");
     public final static Channel RADIO_AIRE = new Channel("Radio Aire", CHANNEL_URI_PREFIX+"kc", "kc");
     public final static Channel RADIO_ALTEA = new Channel("Radio Altea", CHANNEL_URI_PREFIX+"kd", "kd");
     public final static Channel RADIO_CAVELL = new Channel("Radio Cavell", CHANNEL_URI_PREFIX+"kf", "kf");
     public final static Channel RADIO_CITY_96_7 = new Channel("Radio City 96.7", CHANNEL_URI_PREFIX+"kg", "kg");
     public final static Channel RADIO_FIREBIRD = new Channel("Radio Firebird", CHANNEL_URI_PREFIX+"kh", "kh");
     public final static Channel RADIO_GRAPEVINE = new Channel("Radio Grapevine", CHANNEL_URI_PREFIX+"kj", "kj");
     public final static Channel RADIO_GWENDOLEN = new Channel("Radio Gwendolen", CHANNEL_URI_PREFIX+"kk", "kk");
     public final static Channel RADIO_KERRY = new Channel("Radio Kerry", CHANNEL_URI_PREFIX+"km", "km");
     public final static Channel RADIO_LINK = new Channel("Radio Link", CHANNEL_URI_PREFIX+"kn", "kn");
     public final static Channel RADIO_MALDWYN = new Channel("Radio Maldwyn", CHANNEL_URI_PREFIX+"kp", "kp");
     public final static Channel RADIO_NORWICH = new Channel("Radio Norwich", CHANNEL_URI_PREFIX+"kq", "kq");
     public final static Channel RADIO_ORKNEY_MW = new Channel("Radio Orkney MW", CHANNEL_URI_PREFIX+"kr", "kr");
     public final static Channel RADIO_PEMBROKESHIRE = new Channel("Radio Pembrokeshire", CHANNEL_URI_PREFIX+"ks", "ks");
     public final static Channel RADIO_SALOBRENA = new Channel("Radio Salobrena", CHANNEL_URI_PREFIX+"kt", "kt");
     public final static Channel RADIO_SHETLAND_MW = new Channel("Radio Shetland MW", CHANNEL_URI_PREFIX+"kv", "kv");
     public final static Channel RADIO_WAVE = new Channel("Radio Wave", CHANNEL_URI_PREFIX+"kw", "kw");
     public final static Channel RADIO_XL = new Channel("Radio XL", CHANNEL_URI_PREFIX+"kx", "kx");
     public final static Channel REAL_RADIO_NORTH_EAST = new Channel("Real Radio (North East)", CHANNEL_URI_PREFIX+"ky", "ky");
     public final static Channel REAL_RADIO_NORTH_WEST = new Channel("Real Radio (North West)", CHANNEL_URI_PREFIX+"kz", "kz");
     public final static Channel REAL_RADIO_SCOTLAND = new Channel("Real Radio (Scotland)", CHANNEL_URI_PREFIX+"k2", "k2");
     public final static Channel REAL_RADIO_WALES = new Channel("Real Radio (Wales)", CHANNEL_URI_PREFIX+"k4", "k4");
     public final static Channel REAL_RADIO_YORKSHIRE = new Channel("Real Radio (Yorkshire)", CHANNEL_URI_PREFIX+"k5", "k5");
     public final static Channel RED_DOT_RADIO = new Channel("Red Dot Radio", CHANNEL_URI_PREFIX+"k6", "k6");
     public final static Channel RED_FM = new Channel("Red FM", CHANNEL_URI_PREFIX+"k7", "k7");
     public final static Channel REVOLUTION = new Channel("Revolution", CHANNEL_URI_PREFIX+"k8", "k8");
     public final static Channel RIVIERA_RADIO = new Channel("Riviera Radio", CHANNEL_URI_PREFIX+"k9", "k9");
     public final static Channel ROCK_FM = new Channel("Rock FM", CHANNEL_URI_PREFIX+"mb", "mb");
     public final static Channel RTE_LYRIC_FM = new Channel("RTE lyric fm", CHANNEL_URI_PREFIX+"mc", "mc");
     public final static Channel RTE_RADIO_1_FM = new Channel("RTE Radio 1 FM", CHANNEL_URI_PREFIX+"md", "md");
     public final static Channel RTE_RADIO_1_LW = new Channel("RTE Radio 1 LW", CHANNEL_URI_PREFIX+"mf", "mf");
    public final static Channel RTE_RAIDIO_NA_GAELTACHTA = new Channel("RTE Raidió na Gaeltachta", CHANNEL_URI_PREFIX+"mg", "mg");
     public final static Channel RUGBY_FM = new Channel("Rugby FM", CHANNEL_URI_PREFIX+"mh", "mh");
     public final static Channel SABRAS = new Channel("Sabras", CHANNEL_URI_PREFIX+"mj", "mj");
     public final static Channel SIGNAL_ONE_102_6 = new Channel("Signal One 102.6", CHANNEL_URI_PREFIX+"mk", "mk");
     public final static Channel SIGNAL_ONE_FOR_STAFFORD_96_9FM = new Channel("Signal One for Stafford 96.9FM", CHANNEL_URI_PREFIX+"mm", "mm");
     public final static Channel SIGNAL_TWO = new Channel("Signal Two", CHANNEL_URI_PREFIX+"mn", "mn");
     public final static Channel SOUTH_EAST_RADIO = new Channel("South East Radio", CHANNEL_URI_PREFIX+"mq", "mq");
     public final static Channel SOVEREIGN = new Channel("Sovereign", CHANNEL_URI_PREFIX+"mr", "mr");
     public final static Channel SPECTRUM_558_AM_RADIO = new Channel("Spectrum 558 AM Radio", CHANNEL_URI_PREFIX+"ms", "ms");
     public final static Channel SPECTRUM_FM = new Channel("Spectrum FM", CHANNEL_URI_PREFIX+"mt", "mt");
     public final static Channel SPIN_1038 = new Channel("Spin 1038", CHANNEL_URI_PREFIX+"mv", "mv");
     public final static Channel SPIRE_FM_RADIO = new Channel("Spire FM Radio", CHANNEL_URI_PREFIX+"mw", "mw");
     public final static Channel SPIRIT_FM_96_6_AND_102_3 = new Channel("Spirit FM 96.6 and 102.3", CHANNEL_URI_PREFIX+"mx", "mx");
     public final static Channel SPLASH_FM = new Channel("Splash FM", CHANNEL_URI_PREFIX+"my", "my");
     public final static Channel STAR_107_CAMBRIDGE = new Channel("Star 107 (Cambridge)", CHANNEL_URI_PREFIX+"mz", "mz");
     public final static Channel STAR_107_2 = new Channel("Star 107.2", CHANNEL_URI_PREFIX+"m2", "m2");
     public final static Channel STAR_107_7 = new Channel("Star 107.7", CHANNEL_URI_PREFIX+"m4", "m4");
     public final static Channel STAR_RADIO = new Channel("Star Radio", CHANNEL_URI_PREFIX+"m5", "m5");
     public final static Channel STRAY = new Channel("Stray", CHANNEL_URI_PREFIX+"m6", "m6");
     public final static Channel SUN_FM_103_4 = new Channel("Sun FM 103.4", CHANNEL_URI_PREFIX+"m7", "m7");
     public final static Channel SUNRISE_RADIO = new Channel("Sunrise Radio", CHANNEL_URI_PREFIX+"m8", "m8");
     public final static Channel SUNRISE_RADIO_103_2FM = new Channel("Sunrise Radio 103.2FM", CHANNEL_URI_PREFIX+"m9", "m9");
     public final static Channel SUNSHINE_855 = new Channel("Sunshine 855", CHANNEL_URI_PREFIX+"nb", "nb");
     public final static Channel SWANSEA = new Channel("Swansea", CHANNEL_URI_PREFIX+"nc", "nc");
     public final static Channel TAY_AM = new Channel("Tay AM", CHANNEL_URI_PREFIX+"nd", "nd");
     public final static Channel TAY_FM = new Channel("Tay FM", CHANNEL_URI_PREFIX+"nf", "nf");
     public final static Channel TEMPO_FM = new Channel("Tempo FM", CHANNEL_URI_PREFIX+"ng", "ng");
     public final static Channel TFM_RADIO = new Channel("TFM Radio", CHANNEL_URI_PREFIX+"nh", "nh");
     public final static Channel THE_BEE = new Channel("The Bee", CHANNEL_URI_PREFIX+"nj", "nj");
     public final static Channel THE_HITS = new Channel("The Hits", CHANNEL_URI_PREFIX+"nk", "nk");
     public final static Channel THE_SEVERN = new Channel("The Severn", CHANNEL_URI_PREFIX+"nm", "nm");
     public final static Channel TIPP_FM = new Channel("Tipp FM", CHANNEL_URI_PREFIX+"nn", "nn");
     public final static Channel TIPPERARY_MID_WEST_FM_RADIO = new Channel("Tipperary Mid West FM Radio", CHANNEL_URI_PREFIX+"np", "np");
     public final static Channel TODAY_FM = new Channel("Today FM", CHANNEL_URI_PREFIX+"nq", "nq");
     public final static Channel TOTAL_STAR_107_5 = new Channel("Total Star 107.5", CHANNEL_URI_PREFIX+"nr", "nr");
     public final static Channel TOUCH_RADIO_101_6_102_4 = new Channel("Touch Radio 101.6/102.4", CHANNEL_URI_PREFIX+"ns", "ns");
     public final static Channel TOUCH_RADIO_102 = new Channel("Touch Radio 102", CHANNEL_URI_PREFIX+"nt", "nt");
     public final static Channel TOUCH_RADIO_96_2 = new Channel("Touch Radio 96.2", CHANNEL_URI_PREFIX+"nv", "nv");
     public final static Channel TOWER_107_4FM = new Channel("Tower 107.4FM", CHANNEL_URI_PREFIX+"nw", "nw");
     public final static Channel TRAX_FM = new Channel("Trax FM", CHANNEL_URI_PREFIX+"nx", "nx");
     public final static Channel U105_8 = new Channel("U105.8", CHANNEL_URI_PREFIX+"ny", "ny");
     public final static Channel UTD_CHRISTIAN_BROADCASTING = new Channel("Utd Christian Broadcasting", CHANNEL_URI_PREFIX+"nz", "nz");
     public final static Channel VIKING_FM = new Channel("Viking FM", CHANNEL_URI_PREFIX+"n2", "n2");
     public final static Channel WATERFORD = new Channel("Waterford", CHANNEL_URI_PREFIX+"n4", "n4");
     public final static Channel WAVE_102FM = new Channel("Wave 102FM", CHANNEL_URI_PREFIX+"n5", "n5");
     public final static Channel WAVE_105_2FM = new Channel("Wave 105.2FM", CHANNEL_URI_PREFIX+"n6", "n6");
     public final static Channel WAVE_96_4FM = new Channel("Wave 96.4FM", CHANNEL_URI_PREFIX+"n7", "n7");
     public final static Channel WAVES_101_2 = new Channel("Waves 101.2", CHANNEL_URI_PREFIX+"n8", "n8");
     public final static Channel WCR_FM = new Channel("WCR FM", CHANNEL_URI_PREFIX+"n9", "n9");
     public final static Channel WESSEX_FM = new Channel("Wessex FM", CHANNEL_URI_PREFIX+"pb", "pb");
     public final static Channel WEST_FM = new Channel("West FM", CHANNEL_URI_PREFIX+"pc", "pc");
     public final static Channel WEST_SOUND_FM = new Channel("West Sound FM", CHANNEL_URI_PREFIX+"pd", "pd");
     public final static Channel WESTSOUND_AYRSHIRE_1035AM = new Channel("Westsound Ayrshire 1035AM", CHANNEL_URI_PREFIX+"pf", "pf");
     public final static Channel WISH_FM = new Channel("Wish FM", CHANNEL_URI_PREFIX+"pg", "pg");
     public final static Channel WOLF = new Channel("Wolf", CHANNEL_URI_PREFIX+"ph", "ph");
     public final static Channel WORLD_RADIO_NETWORK = new Channel("World Radio Network", CHANNEL_URI_PREFIX+"pj", "pj");
     public final static Channel WYRE_107_2 = new Channel("Wyre 107.2", CHANNEL_URI_PREFIX+"pk", "pk");
     public final static Channel WYVERN_FM = new Channel("Wyvern FM", CHANNEL_URI_PREFIX+"pm", "pm");
     public final static Channel XFM_MANCHESTER = new Channel("Xfm Manchester", CHANNEL_URI_PREFIX+"pn", "pn");
     public final static Channel YORKSHIRE_COAST_BRID_102_4FM = new Channel("Yorkshire Coast (Brid) 102.4FM", CHANNEL_URI_PREFIX+"pp", "pp");
     public final static Channel YORKSHIRE_COAST_96_2FM = new Channel("Yorkshire Coast 96.2FM", CHANNEL_URI_PREFIX+"pq", "pq");
     
     private final String uri;
     private final String title;
 
     private final static List<Channel> VOD_SERVICES = ImmutableList.of(BBC_IPLAYER, HULU, C4_4OD, YOUTUBE, SEESAW);
     private final String key;
     
     private final static Map<String, Channel> uriMap;
     private final static Map<String, Channel> keyMap;
     
     private final static Pattern uriPattern = Pattern.compile("^.*\\/(.+?)\\/?$");
     
     static {
         ImmutableMap.Builder<String, Channel> uriMapBuilder = ImmutableMap.builder();
         ImmutableMap.Builder<String, Channel> keyMapBuilder = ImmutableMap.builder();
         for (Field field: Channel.getClass().getFields()) {
             if (isPublicStaticFinal(field)) {
                 try {
                     Object object = field.get(null);
                     if (object instanceof Channel) {
                         Channel channel = (Channel) object;
                         
                         uriMapBuilder.put(channel.uri(), channel);
                         keyMapBuilder.put(channel.key(), channel);
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         uriMap = uriMapBuilder.build();
         keyMap = keyMapBuilder.build();
     }
 
     public Channel(String title, String uri, String key) {
         this.title = title;
         this.uri = uri;
         Preconditions.checkArgument(key.length() <= MAX_KEY_LENGTH);
         this.key = key;
     }
 
     public String uri() {
         return uri;
     }
 
     public String title() {
         return title;
     }
 
     public String key() {
         return key;
     }
 
     public static Maybe<Channel> fromUri(String uri) {
         Channel channel = uriMap.get(uri);
         if (channel == null) {
             Matcher matcher = uriPattern.matcher(uri);
             if (matcher.matches()) {
                 channel = new Channel(matcher.group(1), uri, matcher.group(1));
             }
         }
         return Maybe.fromPossibleNullValue(channel);
     }
 
     public static Maybe<Channel> fromKey(String key) {
         Channel channel = keyMap.get(key);
         if (channel == null) {
             channel = new Channel(key, CHANNEL_URI_PREFIX+key, key);
         }
         return Maybe.fromPossibleNullValue(channel);
     }
 
     public static List<Channel> fromKeys(Iterable<String> keys) {
         Builder<Channel> builder = ImmutableList.builder();
 
         for (String key : keys) {
             Maybe<Channel> channel = fromKey(key);
             if (channel.hasValue()) {
                 builder.add(channel.requireValue());
             }
         }
 
         return builder.build();
     }
 
     public static List<String> toKeys(Iterable<Channel> channels) {
         Builder<String> keys = ImmutableList.builder();
 
         for (Channel channel : channels) {
             keys.add(channel.key());
         }
 
         return keys.build();
     }
     
     public static Collection<Channel> all() {
     	return keyMap.values();
 	}
 
     public static List<Map<String, ?>> mapList() {
         List<Map<String, ?>> channelList = Lists.newArrayList();
         for (Channel channel : keyMap.values()) {
             channelList.add(channel.toSimpleModel().asMap());
         }
         return channelList;
     }
 
     public static List<Map<String, ?>> mapListWithoutVodServices() {
         List<Map<String, ?>> channelList = Lists.newArrayList();
         for (Channel channel : keyMap.values()) {
             if (!VOD_SERVICES.contains(channel)) {
                 channelList.add(channel.toSimpleModel().asMap());
             }
         }
         return channelList;
     }
 
     @Override
     public SimpleModel toSimpleModel() {
         SimpleModel model = new SimpleModel();
         model.put("name", title);
         model.put("uri", uri);
         model.put("key", key);
 
         return model;
     }
     
     private static boolean isPublicStaticFinal(Field field) {
         int modifiers = field.getModifiers();
         return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Channel) {
             Channel target = (Channel) obj;
             return Objects.equal(key, target.key) && Objects.equal(uri, target.uri);
         }
         return false;
     }
     
     @Override
     public int hashCode() {
         return uri.hashCode();
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this).addValue(key).addValue(uri).toString();
     }
 }
