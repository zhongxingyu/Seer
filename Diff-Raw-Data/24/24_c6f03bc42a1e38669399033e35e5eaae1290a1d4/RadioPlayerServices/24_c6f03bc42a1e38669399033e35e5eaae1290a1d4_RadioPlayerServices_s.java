 package org.atlasapi.feeds.radioplayer;
 
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
import com.google.common.collect.Ranges;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.MorePredicates;
 
 public class RadioPlayerServices {
 
 	public static final Set<RadioPlayerService> services;
 	
 	public static final Map<String, RadioPlayerService> all;
 	
 	public static final Set<RadioPlayerService> untracked;
 
     public static final Map<String, RadioPlayerService> serviceUriToService;
     
     public static final Set<RadioPlayerService> nationalNetworks;
 	
 	static {
 		services = ImmutableSet.<RadioPlayerService> builder().
 			add(new RadioPlayerService(300, "london").withIonServiceId("bbc_london")).
 			add(new RadioPlayerService(301, "berkshire")).
 			add(new RadioPlayerService(302, "bristol")).
 			add(new RadioPlayerService(303, "cambridgeshire").withIonServiceId("bbc_radio_cambridge")).
 			add(new RadioPlayerService(304, "cornwall")).
 			add(new RadioPlayerService(305, "coventry").withIonServiceId("bbc_radio_coventry_warwickshire")).
 			add(new RadioPlayerService(306, "cumbria")).
 			add(new RadioPlayerService(307, "derby")).
 			add(new RadioPlayerService(308, "devon")).
 			add(new RadioPlayerService(309, "essex")).
 			add(new RadioPlayerService(310, "gloucestershire")).
 			add(new RadioPlayerService(311, "guernsey")).
 			add(new RadioPlayerService(312, "herefordandworcester").withIonServiceId("bbc_radio_hereford_worcester")).
 			add(new RadioPlayerService(313, "humberside")).
 			add(new RadioPlayerService(314, "jersey")).
 			add(new RadioPlayerService(315, "kent")).
 			add(new RadioPlayerService(316, "lancashire")).
 			add(new RadioPlayerService(317, "leeds")).
 			add(new RadioPlayerService(318, "leicester")).
 			add(new RadioPlayerService(319, "lincolnshire")).
 			add(new RadioPlayerService(320, "manchester")).
 			add(new RadioPlayerService(321, "merseyside")).
 			add(new RadioPlayerService(322, "newcastle")).
 			add(new RadioPlayerService(323, "norfolk")).
 			add(new RadioPlayerService(324, "northampton")).
 			add(new RadioPlayerService(325, "nottingham")).
 			add(new RadioPlayerService(326, "oxford")).
 			add(new RadioPlayerService(327, "sheffield")).
 			add(new RadioPlayerService(328, "shropshire")).
 			add(new RadioPlayerService(329, "solent")).
 			add(new RadioPlayerService(330, "somerset").withIonServiceId("bbc_radio_somerset_sound")).
 			add(new RadioPlayerService(331, "stoke")).
 			add(new RadioPlayerService(332, "suffolk")).
 			add(new RadioPlayerService(333, "surrey")).
 			add(new RadioPlayerService(334, "sussex")).
 			add(new RadioPlayerService(335, "wiltshire")).
 			add(new RadioPlayerService(336, "york")).
 			add(new RadioPlayerService(337, "tees").withIonServiceId("bbc_tees")).
 			add(new RadioPlayerService(338, "threecounties").withIonServiceId("bbc_three_counties_radio")).
 			add(new RadioPlayerService(339, "wm").withIonServiceId("bbc_wm")).
 			add(new RadioPlayerService(340, "radio1").withServiceUriSuffix("radio1/england").withScheduleUri("http://www.bbc.co.uk/radio1/programmes/schedules/england").withIonServiceId("bbc_radio_one")).
 			add(new RadioPlayerService(341, "1xtra").withIonServiceId("bbc_1xtra")).
 			add(new RadioPlayerService(342, "radio2").withIonServiceId("bbc_radio_two")).
 			add(new RadioPlayerService(343, "radio3").withIonServiceId("bbc_radio_three")).
 			add(new RadioPlayerService(344, "radio4").withServiceUriSuffix("radio4/fm").withScheduleUri("http://www.bbc.co.uk/radio4/programmes/schedules/fm").withIonServiceId("bbc_radio_fourfm")).
 			add(new RadioPlayerService(345, "5live").withIonServiceId("bbc_radio_five_live")).
 			add(new RadioPlayerService(346, "5livesportsextra").withIonServiceId("bbc_radio_five_live_sports_extra")).
 			add(new RadioPlayerService(347, "6music").withIonServiceId("bbc_6music")).
 			add(new RadioPlayerService(349, "asiannetwork").withIonServiceId("bbc_asian_network")).
 			add(new RadioPlayerService(350, "worldservice").withIonServiceId("bbc_world_service")).
 			add(new RadioPlayerService(351, "radioscotland").withServiceUriSuffix("radioscotland/fm").withScheduleUri("http://www.bbc.co.uk/radioscotland/programmes/schedules/fm").withIonServiceId("bbc_radio_scotland_fm")).
 			add(new RadioPlayerService(352, "radionangaidheal").withIonServiceId("bbc_radio_nan_gaidheal")).
 			add(new RadioPlayerService(353, "radioulster").withIonServiceId("bbc_radio_ulster")).
 			add(new RadioPlayerService(354, "radiofoyle").withIonServiceId("bbc_radio_foyle")).
 			add(new RadioPlayerService(355, "radiowales").withServiceUriSuffix("radiowales/fm").withScheduleUri("http://www.bbc.co.uk/radiowales/programmes/schedules/fm").withIonServiceId("bbc_radio_wales_fm")).
 			add(new RadioPlayerService(356, "radiocymru").withIonServiceId("bbc_radio_cymru")).
 			add(new RadioPlayerService(358, "radio4extra").withIonServiceId("bbc_radio_four_extra")).
 			add(new RadioPlayerService(359, "5liveolympicsextra").withIonServiceId("bbc_radio_five_live_olympics_extra")).
 		build();
 		
 		Function<RadioPlayerService, Integer> TO_ID = new Function<RadioPlayerService, Integer>() {
 		    @Override
 		    public Integer apply(@Nullable RadioPlayerService input) {
 		        return input.getRadioplayerId();
 		    }
 		};
 
 		all = Maps.uniqueIndex(services, Functions.compose(Functions.toStringFunction(), TO_ID));
         
         serviceUriToService = Maps.uniqueIndex(services, new Function<RadioPlayerService, String>() {
             @Override
             public String apply(RadioPlayerService input) {
                 return input.getServiceUri();
             }
         });
         
         untracked = ImmutableSet.of(all.get("346"), all.get("358"), all.get("359"));
             
        nationalNetworks = Sets.filter(services, MorePredicates.transformingPredicate(TO_ID, Predicates.or(Ranges.closedOpen(340, 350),Predicates.equalTo(358))));
 	}
 
 }
