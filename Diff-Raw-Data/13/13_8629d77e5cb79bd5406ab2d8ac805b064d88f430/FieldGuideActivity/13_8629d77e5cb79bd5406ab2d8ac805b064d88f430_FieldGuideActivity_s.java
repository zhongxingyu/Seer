 //figure out how to redo the onClick for all (instead of doing one for each)
 
 package org.encorelab.sail.evoroom;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FieldGuideActivity extends Activity {
 	
 // flag to mark button selected in field_guide_area_selector
 	int areaSelectorFlag = 0;
 // flag to mark the selected flora or fauna in field_guide_species_selector
 	String speciesSelected = "";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.field_guide);
         
         setupAreaSelectorView();
         setupSpeciesSelectorView();
         setupSpeciesDisplayView();
 	}
 
 
 	private void setupAreaSelectorView() {
 		Button sundalandFloraButton = (Button) findViewById(R.id.sundalandFloraButton);
 		sundalandFloraButton.setText(Html.fromHtml("<b>Sundaland Flora</b>" +  "<br />" + 
         	"<small>2 million years ago</small>"));
 		sundalandFloraButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 areaSelectorFlag = 1;
                 setupSpeciesSelectorView();
                 changeVis(R.id.field_guide_species_selector);
             }
         });
 		Button sundalandFaunaButton = (Button) findViewById(R.id.sundalandFaunaButton);
 		sundalandFaunaButton.setText(Html.fromHtml("<b>Sundaland Fauna</b>" +  "<br />" + 
         	"<small>2 million years ago</small>"));
 		sundalandFaunaButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	areaSelectorFlag = 2;
                 setupSpeciesSelectorView();
             	changeVis(R.id.field_guide_species_selector);
             }
         });
 		Button borneoFloraButton = (Button) findViewById(R.id.borneoFloraButton);
 		borneoFloraButton.setText(Html.fromHtml("<b>Borneo Flora</b>" +  "<br />" + 
         	"<small>200 years ago</small>"));
 		borneoFloraButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	areaSelectorFlag = 3;
                 setupSpeciesSelectorView();
             	changeVis(R.id.field_guide_species_selector);
             }
         });
 		Button borneoFaunaButton = (Button) findViewById(R.id.borneoFaunaButton);
 		borneoFaunaButton.setText(Html.fromHtml("<b>Borneo Fauna</b>" +  "<br />" + 
         	"<small>200 years ago</small>"));
 		borneoFaunaButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	areaSelectorFlag = 4;
                 setupSpeciesSelectorView();
             	changeVis(R.id.field_guide_species_selector);
             }
         });
 		Button sumatraFloraButton = (Button) findViewById(R.id.sumatraFloraButton);
 		sumatraFloraButton.setText(Html.fromHtml("<b>Sumatra Flora</b>" +  "<br />" + 
         	"<small>200 years ago</small>"));
 		sumatraFloraButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	areaSelectorFlag = 5;
                 setupSpeciesSelectorView();
             	changeVis(R.id.field_guide_species_selector);
             }
         });
 		Button sumatraFaunaButton = (Button) findViewById(R.id.sumatraFaunaButton);
 		sumatraFaunaButton.setText(Html.fromHtml("<b>Sumatra Fauna</b>" +  "<br />" + 
         	"<small>200 years ago</small>"));
 		sumatraFaunaButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	areaSelectorFlag = 6;
                 setupSpeciesSelectorView();
             	changeVis(R.id.field_guide_species_selector);
             }
         });
 		Button allButton = (Button) findViewById(R.id.allButton);
 		allButton.setText(Html.fromHtml("<b>All areas</b>"));
 		allButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	areaSelectorFlag = 7;
                 setupSpeciesSelectorView();
             	changeVis(R.id.field_guide_species_selector);
             	}
         });
 	}
 
 	
 	private void setupSpeciesSelectorView() {
 		Button backButton = (Button) findViewById(R.id.selectorBackButton);
 		backButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	changeVis(R.id.field_guide_area_selector);            }
         });
 
 		// in future we might want to use a custom LayoutInflator here (see junked code at the bottom). This stuff is not dynamic/large enough to warrent it		
 		ImageView image = (ImageView) findViewById(R.id.speciesSelector);   // generic layout, set to correct image during if loop below
 		Button f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14;   // invisible buttons overlaying the image
 		f1 = (Button) findViewById(R.id.f1);									// not all buttons used for each image
 		f2 = (Button) findViewById(R.id.f2);
 		f3 = (Button) findViewById(R.id.f3);
 		f4 = (Button) findViewById(R.id.f4);
 		f5 = (Button) findViewById(R.id.f5);
 		f6 = (Button) findViewById(R.id.f6);
 		f7 = (Button) findViewById(R.id.f7);
 		f8 = (Button) findViewById(R.id.f8);
 		f9 = (Button) findViewById(R.id.f9);
 		f10 = (Button) findViewById(R.id.f10);
 		f11 = (Button) findViewById(R.id.f11);
 		f12 = (Button) findViewById(R.id.f12);
 		f13 = (Button) findViewById(R.id.f13);
 		f14 = (Button) findViewById(R.id.f14);
 		
 		// clear all buttons		
 //		for (int x=0; x<14; x++) {
 //			String btnClear = "f" + Integer.toString(x+1);
 //			.setVisibility(View.GONE);
 			f1.setVisibility(View.GONE);
 			f2.setVisibility(View.GONE);
 			f3.setVisibility(View.GONE);
 			f4.setVisibility(View.GONE);
 			f5.setVisibility(View.GONE);
 			f6.setVisibility(View.GONE);
 			f7.setVisibility(View.GONE);
 			f8.setVisibility(View.GONE);
 			f9.setVisibility(View.GONE);
 			f10.setVisibility(View.GONE);
 			f11.setVisibility(View.GONE);
 			f12.setVisibility(View.GONE);
 			f13.setVisibility(View.GONE);
 			f14.setVisibility(View.GONE);
 //		}
 
 		if (areaSelectorFlag == 1) {
 	        image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_sundaland_plant));
 			f1.setVisibility(View.VISIBLE);
 			f2.setVisibility(View.VISIBLE);
 			f1.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f2.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 
 		}
 		else if (areaSelectorFlag == 2) {
 			image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_sundaland_animal));
 			f1.setVisibility(View.VISIBLE);
 			f2.setVisibility(View.VISIBLE);
 			f3.setVisibility(View.VISIBLE);
 			f4.setVisibility(View.VISIBLE);
 			f1.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f2.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f3.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f4.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 		}
 		else if (areaSelectorFlag == 3) {
 			image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_borneo_plant));
 			f1.setVisibility(View.VISIBLE);
 			f2.setVisibility(View.VISIBLE);
 			f3.setVisibility(View.VISIBLE);
 			f1.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f2.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f3.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 		}
 		else if (areaSelectorFlag == 4) {
 			image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_borneo_animal));
 			f1.setVisibility(View.VISIBLE);
 			f2.setVisibility(View.VISIBLE);
 			f3.setVisibility(View.VISIBLE);
 			f4.setVisibility(View.VISIBLE);
 			f5.setVisibility(View.VISIBLE);
 			f6.setVisibility(View.VISIBLE);
 			f7.setVisibility(View.VISIBLE);
 			f1.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f2.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f3.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f4.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f5.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f6.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f7.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 
 		}
 		else if (areaSelectorFlag == 5) {
 			image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_sumatra_plant));
 			f1.setVisibility(View.VISIBLE);
 			f2.setVisibility(View.VISIBLE);
 			f3.setVisibility(View.VISIBLE);
 			f4.setVisibility(View.VISIBLE);
 			f5.setVisibility(View.VISIBLE);
 			f1.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f2.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f3.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f4.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f5.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "";
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 		}
 		else if (areaSelectorFlag == 6) {
 			image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_sumatra_animal));
 			f1.setVisibility(View.VISIBLE);
 			f2.setVisibility(View.VISIBLE);
 			f3.setVisibility(View.VISIBLE);
 			f4.setVisibility(View.VISIBLE);
 			f5.setVisibility(View.VISIBLE);
 			f6.setVisibility(View.VISIBLE);
 			f1.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "striped_rabbit";
 	            	setupSpeciesDisplayView();
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f2.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "leaf_monkey";
 	            	setupSpeciesDisplayView();
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f3.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "malayan_tapir";
 	            	setupSpeciesDisplayView();
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f4.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "sumatran_orangutan";
 	            	setupSpeciesDisplayView();
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f5.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "sunda_leopard";
 	            	setupSpeciesDisplayView();
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 			f6.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	speciesSelected = "sumatran_rhino";
 	            	setupSpeciesDisplayView();
 	            	changeVis(R.id.field_guide_species_display);
 	            	}
 	        });
 		}
 		else {
 			image.setImageDrawable(getResources().getDrawable(R.drawable.area_box_all));
 		}
 	}
     
 	private void setupSpeciesDisplayView() {
 		Button backButton = (Button) findViewById(R.id.displayBackButton);
 		backButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	changeVis(R.id.field_guide_species_selector);
             }
         });
 
 		ImageView speciesPhoto = (ImageView) findViewById(R.id.speciesPhoto);
 		ImageView speciesDrawn = (ImageView) findViewById(R.id.speciesDrawn);
		TextView scientificNameContent, familyContent, descriptionContent, habitatContent, prevalenceContent, interestingContent, sourcesContent; 
 		scientificNameContent = (TextView) findViewById(R.id.scientificNameContent);
 		familyContent = (TextView) findViewById(R.id.familyContent);
 		descriptionContent = (TextView) findViewById(R.id.descriptionContent);
 		habitatContent = (TextView) findViewById(R.id.habitatContent);
 		prevalenceContent = (TextView) findViewById(R.id.prevalenceContent);
		interestingContent = (TextView) findViewById(R.id.interestingContent);
 		sourcesContent = (TextView) findViewById(R.id.sourcesContent);
 
 		scientificNameContent.setText(" ");
 		familyContent.setText(" ");
 		descriptionContent.setText(" ");
 		habitatContent.setText(" ");
 		prevalenceContent.setText(" ");
		interestingContent.setText(" ");
 		sourcesContent.setText(" ");
 		
 		if (speciesSelected.equals("rafflesia_arnoldii")) {
 			
 		}
 		if (speciesSelected.equals("amorphophallus_titanum")) {
 			
 		}
 		if (speciesSelected.equals("ficus_elastica")) {
 			
 		}
 		if (speciesSelected.equals("ficus_benjamina")) {
 			
 		}
 		if (speciesSelected.equals("ficus_microcarpa")) {
 			
 		}
 		if (speciesSelected.equals("tetrastigma")) {
 			
 		}
 		if (speciesSelected.equals("strangler_fig")) {
 			
 		}
 		if (speciesSelected.equals("amorphophallus_titanum")) {
 			
 		}
 		
 		if (speciesSelected.equals("striped_rabbit")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Nesolagus netscheri");
 			familyContent.setText("Leporidae");
 			descriptionContent.setText("The Sumatran Striped Rabbit is usually about 40 cm (1 ft, 4 in) long. It is gray with brown stripes, with a red tail and rump, and the underside is white. It lives in forests at altitudes of 600-1400 metres. It is nocturnal, resting in the burrows of other animals. It usually eats the stalk and leaves of understory plants, but captive rabbits ate grain and tropical fruits.");
 			habitatContent.setText("Exists in forests along the mountainous edge of Sumatra");
 			prevalenceContent.setText("Critically endangered because of loss of habitat");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("leaf_monkey")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Presbytis melalophos");
 			familyContent.setText("Cercopithecidae");
 			descriptionContent.setText("The pollex (thumb) is reduced in this species. The orbits are widely spaced and the hindlimbs are longer as compared to the forelimbs. The average body mass for an adult male mitered leaf-monkey is around 5.9 kilograms, and for the female it is around 5.8 kilograms. This species has a sacculated stomach to assist in the breakdown of cellulose. Infants have a white pelage color with a dark stripe down the back and across the shoulders.");
 			habitatContent.setText("This species lives in primary lowland rainforests, hill forests, inland secondary forests, and submontane forests.");
 			prevalenceContent.setText("The species is considered Endangered due to ongoing population declines driven by habitat conversion and illegal collecting for the pet trade. Although forest loss has probably exceeded 70% over 3 generations (30 years approximately), the subspecies is tolerant to some degree of conversion such that it is likely to persist in fragmented landscapes over the medium term.");
 			sourcesContent.setText("Primata");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("malayan_tapir")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Tapirus indicus");
 			familyContent.setText("Tapiridae");
 			descriptionContent.setText(Html.fromHtml("Also known as the Asian Tapir, the Malayan Tapir (Tapirus indicus) is the largest of the four species of tapir and the only one native to Asia. In the Malay language, the tapir is commonly referred to as \"cipan\", \"tenuk\" or \"badak tampong\".<br/>Notable markings: light colored patch which extends from its shoulders to its rear; the rest of its hair is black and tips of ears white; The pattern is for camouflage;<br/>-Grows to 6-8 feet (1.8 to 2.4 m) in length; stand 3-3.5 feet (90 to 107 cm tall); Females are larger than males<br/>-Poor eyesight but excellent sense of smell<br/>-Communicate with high-pitched squeaks and whistles<br/>-Live near water (bathe and swim often)<br/>-Can climb steep slopes<br/>-Active at night, eating soon after sunset or before sunrise"));
 			habitatContent.setText("Found in lowland rainforests");
 			prevalenceContent.setText("Deforestation for agricultural purposes, flooding caused by damming of rivers for hydroelectric projects and illegal trade");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("sumatran_orangutan")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Pongo abelii");
 			familyContent.setText("Hominidae");
 			descriptionContent.setText(Html.fromHtml("One of two species of orangutans - it is rarer and smaller than Bornean orangutan. The Sumatran and Bornean species diverged 400,000 years ago, but the two islands split at least 21,000 years ago.<br/>-Red-orange coat of hair, and white around the mouth<br/>-Grows to height of 1.4 metres (4.6 ft) tall and 90 kilograms (200 lbs) in males; females smaller 90 centimetres (3.0 ft) and 45 kilograms (99 lbs)<br/>-Tends to be more frugivorous (figs and jackfruits) and insectivorous; it will also eat birds' eggs and small vertebrates<br/>-Observed using tools (breaking tree branches and uses them to dig in tree holes for termites or poke bees nest walls for honey, or to eat fruit<br/>-More social than Bornean orangutan"));
 			habitatContent.setText("Lives in rainforest and swamps of Sumatra (Suaq Balimbing swamp); largest of the tree-dwelling primates");
 			prevalenceContent.setText("7,3000 Sumatran organutans still live in the wild; critically endangered; no more than 6-7 orangutan's per square kilometer");
 			sourcesContent.setText("Wikipedia, Nature wildlife, Sumatra Orangutan Chapter 2, Know your Orangutan, Primatology");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("sunda_leopard")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Neofelis diardi");
 			familyContent.setText("Felidae");
 			descriptionContent.setText("The Sunda Clouded Leopard, also known as the Sundaland Clouded Leopard and the Bornean clouded leopard is a medium-sized wild cat found in Borneo and Sumatra. The Sunda clouded leopard is the largest felid in Borneo, and has a stocky build, weighing around 12 to 25 kg (26 to 55 lb). The canine teeth are two inches long, which, in proportion to the skull length, are longer than those of any other extant feline. Its tail can grow to be as long as its body, aiding balance. Its coat is marked with irregularly-shaped, dark-edged ovals which are said to be shaped like clouds, hence its common name. In 2006, it was classified as a separate species, distinct from its continental relative Neofelis nebulosa.");
 			habitatContent.setText("Sunda clouded leopards are largely unknown because of their secretive nature. In Borneo, they occur in lowland rainforest, and at lower density, in logged forest. Records in Borneo are below 1,500 m (4,900 ft). In Sumatra, they appear to be more abundant in hilly, montane areas. It is unknown if there are still Sunda clouded leopards on the small Batu Islands close to Sumatra.");
 			prevalenceContent.setText("In 2008, the IUCN classified the species as vulnerable, with a total effective population size suspected to be fewer than 10,000 mature individuals, and a decreasing population trend.");
 			sourcesContent.setText("New Species Declared: Clouded Leopard found on Borneo and Sumatra, Wikipedia");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("sumatran_rhino")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Dicerorhinus sumatrensis");
 			familyContent.setText("Rhinocerotidae");
 			descriptionContent.setText("The Sumatran rhino lives an estimated 30-45 years in the wild. They are solitary creatures except when pairing prior to mating and during child rearing. They are most active when eating at dawn or just after dusk, loves to wallow in mud baths to cool down and rest. In rainy seasons, the rhinos will move to higher elevations and in cooler months in the lower areas. The Sumatran Rhinos are fast and agile; they climb mountains easily and comfortably traverses steep slopes and riverbanks.");
 			habitatContent.setText("Sumatran Rhinos live in both lowland and highland secondary rainforest, swamps and the cloud forest. They prefer hilly areas close to water, particularly steep upper valleys with a lot of undergrowth. Known to be in Sumatra, Sabah and Borneo (used to live more places; only six known communities contain Sumatran Rhinos)");
 			prevalenceContent.setText("Fewer than 275 individuals remain; critically endangered primarily due to illegal poaching; rhino horns used in amulets and protection; rhino horn now seen as aphrodisiac (traditional Chinese medicine never used it for this purpose); Also hardwood from the rainforest causing rhino habitat depletion.");
			interestingContent.setText("The most vocal of rhinoceros species; unknown purpose of vocalization (whistle blow)");
 			sourcesContent.setText("Wikipedia");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("sumatran_tiger")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Panthera tigris sumatrae");
 			familyContent.setText("Felidae");
 			descriptionContent.setText(Html.fromHtml("The Sumatran tiger is the smallest of the tiger subspecies. Sumatran male tigers average 8 feet (2.438m) in length and weigh about 265 lbs. (120.2kg), while females average 7 feet (2.134m) in length and weigh about 200 lbs (90.718kg). Their small size helps them move through the jungle, and stripes and patterning is adapted for camouflage in the tall grass and underbush. Males are more bearded and maned (neck and cheek is developed).<br/>-They have webbed toes and are fast swimmers - they run their prey into the water<br/>-White spots on the back of tiger's ears are called \"eye spots\" or \"predator spots\". These spots are believed to function as false eyes as well as to make it look larger to any predator approaching from behind. This is particularly helpful in keeping cubs safe.<br/>-Sumatra provides a varying landscape in which this subspecies lives. From peat-moss forest, submountain and mountain forest as well as lowland forest. As a result, the Sumatran Tiger's coat has evolved slightly different from the other tiger subspecies. This evolution adaptation to their environment give the them the darkest coat of all tigers. The Sumatran Tiger has wide black stripes that are closely spaced, sometimes doubled up on each other. These stripes also run down the animal's forelegs, a distinction setting them apart from the Siberian Tiger.<br/>-They prey on larger ungulates, Wild Boar, Malayan Tapir, deer, smaller animals fowl, monkeys, fish, and mice (when desperate for food)."));
 			habitatContent.setText("Only found on Indonesian island of Sumatra, lowland forest to sub mountain and mountain forest including some peat moss forests; The agriculture of Sumatra has changed and thus caused problems for the tigers.");
 			prevalenceContent.setText("Continual loss of habitat (due for the deforestation from the production of palm oil), agricultural growth has made habitat for tigers fragmented. There are no more than 500 tigers left in the wild.");
 			sourcesContent.setText("Wikipedia, Tiger Homes");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("fig_wasp_sumatra")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("Eupristina verticillata");
 			familyContent.setText("Agaonidae");
 			descriptionContent.setText("Also known as the Chinese banyan pollinator wasp, the Eupristina verticillata is a sub species of fig wasps in from the Agaonidae family. They pollinate figs or are otherwise associated with figs in a coevolutional relationship that has been developing for at least 80 million years.");
 			sourcesContent.setText("Bugs in the news, Wikipedia");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("fig_wasp_borneo")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("");
 			familyContent.setText("");
 			descriptionContent.setText("");
 			habitatContent.setText("");
 			prevalenceContent.setText("");
 			sourcesContent.setText("");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("rhino_hornbill")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("");
 			familyContent.setText("");
 			descriptionContent.setText("");
 			habitatContent.setText("");
 			prevalenceContent.setText("");
 			sourcesContent.setText("");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("rhinoplax_vigil")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("");
 			familyContent.setText("");
 			descriptionContent.setText("");
 			habitatContent.setText("");
 			prevalenceContent.setText("");
 			sourcesContent.setText("");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("grey_hornbill")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("");
 			familyContent.setText("");
 			descriptionContent.setText("");
 			habitatContent.setText("");
 			prevalenceContent.setText("");
 			sourcesContent.setText("");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("proboscis_monkey")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("");
 			familyContent.setText("");
 			descriptionContent.setText("");
 			habitatContent.setText("");
 			prevalenceContent.setText("");
 			sourcesContent.setText("");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 		if (speciesSelected.equals("borneo_orangutan")) {
 			speciesPhoto.setImageDrawable(getResources().getDrawable(R.drawable.striped_rabbit));
 			scientificNameContent.setText("");
 			familyContent.setText("");
 			descriptionContent.setText("");
 			habitatContent.setText("");
 			prevalenceContent.setText("");
 			sourcesContent.setText("");
 			speciesDrawn.setImageDrawable(getResources().getDrawable(R.drawable.drawn_sumatran_rabbit));
 		}
 
 
 	}
 
 	private void changeVis(int vis) {
         findViewById(R.id.field_guide_area_selector).setVisibility(View.GONE);
     	findViewById(R.id.field_guide_species_selector).setVisibility(View.GONE);
         findViewById(R.id.field_guide_species_display).setVisibility(View.GONE);
         findViewById(vis).setVisibility(View.VISIBLE);
 	}
 	
 }
 
 
 
 
 //THIS IS AN EXAMPLE OF HOW TO DO THE onClick CORRECTLY FOR MULTIPLE LISTENERS
 //
 //@Override
 //public void onClick(View v) {
 //	// TODO Auto-generated method stub
 //	switch(v.getId()){  
 //	case R.id.sundalandFloraButton:
 //        findViewById(R.id.field_guide_species_selector).setVisibility(View.VISIBLE);
 //        findViewById(R.id.field_guide_species_display).setVisibility(View.GONE);
 //        findViewById(R.id.field_guide_area_selector).setVisibility(View.GONE);
 //        break;
 //	case R.id.sundalandFaunaButton:
 //		// launch activity;
 //		sundalandFaunaButton.setText("SundaFauna");
 //		break;
 //	case R.id.borneoFloraButton:
 //		// launch activity;
 //		borneoFloraButton.setText("bornFlora");
 //		break;
 //	case R.id.borneoFaunaButton:
 //		// launch activity;
 //		borneoFaunaButton.setText("bornFauna");
 //		break;
 //	case R.id.sumatraFloraButton:
 //		// launch activity;
 //		sumatraFloraButton.setText("sumatraFlora");
 //		break;
 //	case R.id.sumatraFaunaButton:
 //		// launch activity;
 //		sumatraFaunaButton.setText("sumatraFauna");
 //		break;
 //	case R.id.allButton:
 //		// Launch activity;
 //    	allButton.setText("all");
 //
 //	}
 //}
 
 
 //EXAMPLE CODE FROM DECLARING BUTTONS INLINE, AND THEN STYLING THEM
 //
 //Button topLeftButton = (Button) findViewById(R.id.topLeftButton);
 //Button topRightButton = (Button) findViewById(R.id.topRightButton);
 //Button f3 = (Button) findViewById(R.id.genericLeftButton);
 //f3.setHeight(291);
 //f3.setWidth(286);
 //Button f4 = (Button) findViewById(R.id.genericRightButton);
 //f4.setHeight(291);
 //f4.setWidth(286);
 //
 //RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
 //        ViewGroup.LayoutParams.WRAP_CONTENT);
 //p1.addRule(RelativeLayout.BELOW, R.id.topLeftButton);
 //p1.addRule(RelativeLayout.ALIGN_LEFT, R.id.topLeftButton);
 //f3.setLayoutParams(p1);
 //
 //RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
 //        ViewGroup.LayoutParams.WRAP_CONTENT);
 //p2.addRule(RelativeLayout.BELOW, R.id.topRightButton);
 //p2.addRule(RelativeLayout.ALIGN_RIGHT, topRightButton.getId());
 //f4.setLayoutParams(p2);
 //
 //Button f5 = new Button(this);
 //f5.setHeight(291);
 //f5.setWidth(286);
 //RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
 //        ViewGroup.LayoutParams.WRAP_CONTENT);
 //p3.addRule(RelativeLayout.BELOW, f3.getId());
 //p3.addRule(RelativeLayout.ALIGN_LEFT, f3.getId());
 //f5.setLayoutParams(p3);
 //
 //Button f6 = new Button(this);
 //f6.setHeight(291);
 //f6.setWidth(286);
 //RelativeLayout.LayoutParams p4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
 //        ViewGroup.LayoutParams.WRAP_CONTENT);
 //p4.addRule(RelativeLayout.BELOW, f4.getId());
 //p4.addRule(RelativeLayout.ALIGN_RIGHT, f4.getId());
 //f6.setLayoutParams(p4);
 //
 //LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout02);
 //layout.addView(f5);
