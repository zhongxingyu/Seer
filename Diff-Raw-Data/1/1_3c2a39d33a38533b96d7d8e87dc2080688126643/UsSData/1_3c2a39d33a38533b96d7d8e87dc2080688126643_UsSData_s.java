 package data.scripts;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class UsSData {     
     
     public static String player_id;
     
     public static final Map All_Variants, Variants_PIR, Variants_IND, Variants_HEG, Variants_TT, Variants_PCK, Variants_JP, Variants_SHI, Variants_BR, HE_F, HE_D, HE_C, HE_W, HE_CRR,
             HE_TR, HE_CS, PPSHI_CRR, PPSHI_CS, PPSHI_C, PPSHI_D, PPSHI_F, PP_CRR, PP_TR, PP_CS, PP_W, PP_C, PP_D, PP_F, PPSHI_TR, PPSHI_W, BR_U, BR_TR, BR_CRR, BR_W, BR_CS, BR_C, BR_D, BR_F,
             SHI_U, SHI_TR, SHI_CRR, SHI_W, SHI_C, SHI_CS, SHI_D, SHI_F, JP_U, JP_W, JP_TR, JP_CS, JP_C, JP_D, JP_F, PCK_W, PCK_F, PCK_D, PCK_C, PCK_CS, CIV_LG, CIV_W, IND_TR, IND_W,
             IND_CRR, IND_CS, IND_C, IND_F, IND_D, SIN_TR, SIN_CRR, SIN_W, SIN_CS, SIN_C, SIN_D, SIN_F, TT_TR, TT_CRR, TT_W, TT_CS, TT_C, TT_D, TT_F, PPBR_TR, PPBR_CRR, PPBR_W,
             PPBR_CS, PPBR_C, PPBR_D, PPBR_F, PP_B, ALL_D_FINAL, ALL_W_FINAL, ALL_F_FINAL, ALL_C_FINAL, ALL_CS_FINAL, NOM_F, NOM_D, NOM_C, NOM_CS, NOM_W, PCK_TR;
     
     public static final List PCK_SW_FINAL, PCK_MW_FINAL, PCK_LW_FINAL, JP_SW_FINAL, JP_MW_FINAL, JP_LW_FINAL, SHI_MW_FINAL, SHI_SW_FINAL, SHI_LW_FINAL, BR_SW_FINAL, BR_MW_FINAL, BR_LW_FINAL,
             LT_SW_FINAL, LT_MW_FINAL, LT_LW_FINAL,MT_SW_FINAL, MT_MW_FINAL, MT_LW_FINAL, HT_SW_FINAL, HT_MW_FINAL, HT_LW_FINAL, ALL_SW_FINAL, ALL_MW_FINAL, ALL_LW_FINAL;
     
     public static final String [] Systems = {"Corvus","Askonia","Anar","Breh'Inni","Canis","Gneiss"};
      
     public static final String [] LOWTECH_SW = {"vulcan","bomb","mininglaser","lightmortar","lightdualmg","lightmg","lightdualac","lightac","fragbomb","clusterbomb","annihilator"};
     public static final String [] LOWTECH_MW = {"miningblaster","annihilatorpod","heavymg","flak","arbalest","heavyac"};
     public static final String [] LOWTECH_LW = {"hellbore","hephag"};
         
     public static final String [] MIDTECH_SW = {"swarmer","lightneedler","pdlaser","lrpdlaser","irpulse","lightag"};
     public static final String [] MIDTECH_MW = {"shredder","hveldriver","heavyneedler","heavymauler","dualflak","chaingun"};
     public static final String [] MIDTECH_LW = {"gauss","multineedler","mark9"};
 
     public static final String [] HIGHTECH_SW = {"taclaser","railgun","ioncannon","pdburst","atropos","atropos_single","amblaster"};
     public static final String [] HIGHTECH_MW = {"phasebeam","pulselaser","heavyburst","heavyblaster","gravitonbeam","phasecl"};   
     public static final String [] HIGHTECH_LW = {"mjolnir","hurricane","autopulse","guardian","hil","plasma","tachyonlance"};
     
     public static final String [] MISSILES_SW = {"sabot","sabot_single","heatseeker","harpoon_single","harpoon","reaper"};
     public static final String [] MISSILES_MW = {"pilum","typhoon","salamanderpod","sabotpod","harpoonpod"};   
     public static final String [] MISSILES_LW = {"cyclone",};
     
     public static final String [] PCK_MW = {"pack_ripsaw"};
     
     public static final String [] JP_SW = {"junk_pirates_grapeshot_s","junk_pirates_lexcimer","junk_pirates_scatterpd"};
     public static final String [] JP_MW = {"junk_pirates_cutlass","junk_pirates_grapeshot_m"};
     public static final String [] JP_LW = {"junk_pirates_grapeshot"};
     
     public static final String [] SHI_SW = {"ms_cepc","ms_pdcepc","ms_shrike_rack","ms_shrike_single","ms_blackcap_6x","ms_blackcap_3x","ms_tusk_rack","ms_tusk_single"};
     public static final String [] SHI_MW = {"ms_blackcap_pod","ms_shrike_pod","ms_scattercepc","ms_mcepc","ms_chaingang","ms_shrike_pod"};
     public static final String [] SHI_LW = {"ms_wamgun"};
  
     public static final String [] BR_SW = {"br_fpde","brdy_volley","brdy_2xfury","brdy_fury","brvulcan","brdy_ag","achilles_mrm","brdy_quill","brdy_ac","brdy_argussmall","brdy_linear"};
     public static final String [] BR_MW = {"achillespod","brdy_plasma","brburst","brdy_dualac","brdy_solenoid","brdy_squallgun","br_pde","br_iwbattery","brdy_galecannon"};
     public static final String [] BR_LW = {"brdy_solenoidlarge","brdy_squallbattery"};
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(HIGHTECH_SW));
         SW_list.addAll(Arrays.asList(MIDTECH_SW));
         SW_list.addAll(Arrays.asList(LOWTECH_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         SW_list.addAll(Arrays.asList(JP_SW));
         SW_list.addAll(Arrays.asList(SHI_SW));
         SW_list.addAll(Arrays.asList(BR_SW));
         ALL_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(HIGHTECH_MW));
         MW_list.addAll(Arrays.asList(MIDTECH_MW));
         MW_list.addAll(Arrays.asList(LOWTECH_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         MW_list.addAll(Arrays.asList(PCK_MW));
         MW_list.addAll(Arrays.asList(JP_MW));
         MW_list.addAll(Arrays.asList(SHI_MW));
         MW_list.addAll(Arrays.asList(BR_MW));        
         ALL_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(HIGHTECH_LW));
         LW_list.addAll(Arrays.asList(MIDTECH_LW));
         LW_list.addAll(Arrays.asList(LOWTECH_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         LW_list.addAll(Arrays.asList(JP_LW));
         LW_list.addAll(Arrays.asList(SHI_LW));
         LW_list.addAll(Arrays.asList(BR_LW));
         ALL_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
         
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(HIGHTECH_SW));
         SW_list.addAll(Arrays.asList(HIGHTECH_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         HT_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(HIGHTECH_MW));
         MW_list.addAll(Arrays.asList(HIGHTECH_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         HT_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(HIGHTECH_LW));
         LW_list.addAll(Arrays.asList(HIGHTECH_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         HT_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(LOWTECH_SW));
         SW_list.addAll(Arrays.asList(LOWTECH_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         LT_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(LOWTECH_MW));
         MW_list.addAll(Arrays.asList(LOWTECH_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         LT_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(LOWTECH_LW));
         LW_list.addAll(Arrays.asList(LOWTECH_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         LT_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(MIDTECH_SW));
         SW_list.addAll(Arrays.asList(MIDTECH_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         MT_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(MIDTECH_MW));
         MW_list.addAll(Arrays.asList(MIDTECH_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         MT_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(MIDTECH_LW));
         LW_list.addAll(Arrays.asList(MIDTECH_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         MT_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(HIGHTECH_SW));
         SW_list.addAll(Arrays.asList(BR_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         BR_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(HIGHTECH_MW));
         MW_list.addAll(Arrays.asList(BR_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         BR_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(HIGHTECH_LW));
         LW_list.addAll(Arrays.asList(BR_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         BR_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(HIGHTECH_SW));
         SW_list.addAll(Arrays.asList(SHI_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         SHI_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(HIGHTECH_MW));
         MW_list.addAll(Arrays.asList(SHI_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         SHI_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(HIGHTECH_LW));
         LW_list.addAll(Arrays.asList(SHI_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         SHI_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(MIDTECH_SW));
         SW_list.addAll(Arrays.asList(LOWTECH_SW));
         SW_list.addAll(Arrays.asList(JP_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         JP_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(MIDTECH_MW));
         MW_list.addAll(Arrays.asList(LOWTECH_MW));
         MW_list.addAll(Arrays.asList(JP_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         JP_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(MIDTECH_LW));
         LW_list.addAll(Arrays.asList(LOWTECH_LW));
         LW_list.addAll(Arrays.asList(JP_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         JP_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     static
     {
         List SW_list = new ArrayList();
         SW_list.addAll(Arrays.asList(MIDTECH_SW));
         SW_list.addAll(Arrays.asList(LOWTECH_SW));
         SW_list.addAll(Arrays.asList(MISSILES_SW));
         PCK_SW_FINAL = Collections.unmodifiableList(SW_list);
         
         List MW_list = new ArrayList();
         MW_list.addAll(Arrays.asList(MIDTECH_MW));
         MW_list.addAll(Arrays.asList(LOWTECH_MW));
         MW_list.addAll(Arrays.asList(PCK_MW));
         MW_list.addAll(Arrays.asList(MISSILES_MW));
         PCK_MW_FINAL = Collections.unmodifiableList(MW_list);
         
         List LW_list = new ArrayList();
         LW_list.addAll(Arrays.asList(MIDTECH_LW));
         LW_list.addAll(Arrays.asList(LOWTECH_LW));
         MW_list.addAll(Arrays.asList(MISSILES_LW));
         PCK_LW_FINAL = Collections.unmodifiableList(LW_list);
     }
     
     
     
 //    public static final String [] IF_SW = {"HV50"};
 //    public static final String [] IF_MW = {"thunderchief","riptide","massdriver","lancer_single","HV75", "harpypod"};
 //    public static final String [] IF_LW = {"cain","citadelpd","hadron","HV100","lancer_dual","lancer","locktide","nucleon","omega","peacemaker","piranha"};
 
 //    public static final String [] GD_SW = {"gedune_flarepd","gedune_hipdlaser"};
 //    public static final String [] GD_MW = {"gedune_reithe","gedune_repeater","gedune_scythe"};
 //    public static final String [] GD_LW = {"gedune_plasma", "gedune_maelstrom"};   
         
 //    public static final String [] NC_SW = {"neutrino_pulsar","neutrino_lightphoton","neutrino_disruptor"};
 //    public static final String [] NC_MW = {"neutrino_advancedtorpedo","neutrino_antiproton","neutrino_darkmatterbeamcannon","neutrino_derp_launcher","neutrino_dualpulsar","neutrino_photongun","neutrino_freightergun","neutrino_photontorpedo","neutrino_pulsebeam","neutrino_neutronpulse","neutrino_neutronpulseheavy",};
 //    public static final String [] NC_LW = {"neutrino_dualpulsebeam","neutrino_fusionlance","neutrino_graviton_inverter","neutrino_heavyphotonrepeater","neutrino_heavypulsar","neutrino_javelin","neutrino_neutronlance","neutrino_particlecannonarray","neutrino_tractorbeam","neutrino_unstable_photon","neutrino_XLadvancedtorpedo","neutrino_neutronpulsebattery",};
         
 //    public static final String [] TL_SW = {"thule_light_hvpc","thule_bulwark_srm_launcher","thule_light_hunker", "thule_clusterbomb"};
 //    public static final String [] TL_MW = {"thule_heavy_hvpc","thule_barbarossa","thule_bulwark_srm_pod","thule_heavyslugger","thule_achilles"};
 //    public static final String [] TL_LW = {"thule_meteor_launcher","thule_humbolt",};
   
     static 
     {
     Map F = new HashMap();
     F.put("hound_Hull", 1f);
     F.put("lasher_Hull", 1f);
     HE_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("enforcer_Hull", 2f);
     D.put("condor_Hull", 1f);
     D.put("mule_Hull", 0.5f);
     HE_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("dominator_Hull", 1f);
     HE_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("onslaught_Hull", 1f);
     HE_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("broadsword_wing", 2f);
     W.put("piranha_wing", 1f);
     W.put("talon_wing", 1f);
     W.put("warthog_wing", 2f);
     HE_W = Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("condor_Hull", 1f);
     HE_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("mule_Hull", 1f);
     TR.put("phaeton_Hull", 1f);
     TR.put("tarsus_Hull", 2f);
     TR.put("atlas_Hull", 0.33f);
     TR.put("dram_Hull", 2f);
     TR.put("valkyrie_Hull", 1f);
     HE_TR = Collections.unmodifiableMap(TR);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("hound_Hull", 1f);
     F.put("lasher_Hull", 1f);
     PP_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("enforcer_Hull", 1f);
     D.put("condor_Hull", 1f);
     D.put("mule_Hull", 2f);
     D.put("buffalo2_Hull", 3f);
     PP_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("dominator_Hull", 1f);
     C.put("venture_Hull", 3f);
     PP_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("conquest_Hull", 1f);
     PP_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("piranha_wing", 1f);
     W.put("talon_wing", 1.5f);
     PP_W = Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("condor_Hull", 2f);
     CRR.put("venture_Hull", 1f);
     PP_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("mule_Hull", 2f);
     TR.put("venture_Hull", 1f);
     TR.put("tarsus_Hull", 1f);
     TR.put("buffalo2_Hull", 2f);
     PP_TR = Collections.unmodifiableMap(TR);
     
     Map B = new HashMap();
     B.put("buffalo2_Hull", 1f);
     PP_B = Collections.unmodifiableMap(B);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("hound_Hull", 1f);
     F.put("lasher_Hull", 1f);
     F.put("ms_seski_Hull", 0.25f);
     F.put("ms_enlil_Hull", 0.375f);
     F.put("ms_shamash_Hull", 0.125f);
     F.put("ms_inanna_Hull", 0.25f);
     PPSHI_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("enforcer_Hull", 1f);
     D.put("condor_Hull", 1f);
     D.put("mule_Hull", 2f);
     D.put("buffalo2_Hull", 3f);
     D.put("ms_morningstar_Hull", 1.5f);
     D.put("ms_sargasso_Hull", 0.5f);
     PPSHI_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("dominator_Hull", 1f);
     C.put("venture_Hull", 3f);
     PPSHI_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("conquest_Hull", 1f);
     PPSHI_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("piranha_wing", 1f);
     W.put("talon_wing", 1.5f);
     W.put("ms_skinwalker_wing", 0.25f);
     W.put("ms_neriad_wing", 0.5f);
     W.put("ms_raksasha_wing", 0.25f);
     PPSHI_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("condor_Hull", 2f);
     CRR.put("venture_Hull", 1f);
     CRR.put("ms_sargasso_Hull", 1f);
     PPSHI_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("mule_Hull", 2f);
     TR.put("venture_Hull", 1f);
     TR.put("tarsus_Hull", 1f);
     TR.put("buffalo2_Hull", 2f);
     TR.put("ms_lambent_Hull", 1f);
     TR.put("ms_solidarity_Hull", 1f);
     PPSHI_TR = Collections.unmodifiableMap(TR);
     } 
     
     static 
     {
     Map F = new HashMap();
     F.put("hound_Hull", 1f);
     F.put("lasher_Hull", 1f);
     F.put("brdy_scarab_Hull", 0.25f);
     F.put("brdy_mantis_Hull", 0.125f);
     F.put("brdy_locust_Hull", 0.25f);
     F.put("brdy_robberfly_Hull", 0.25f);
     PPBR_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("enforcer_Hull", 1f);
     D.put("condor_Hull", 1f);
     D.put("mule_Hull", 2f);
     D.put("buffalo2_Hull", 3f);
     D.put("brdy_cetonia_Hull", 0.25f);
     D.put("brdy_desdinova_Hull", 0.75f);
     D.put("brdy_gonodactylus_Hull", 0.5f);
     D.put("brdy_typheus_Hull", 0.5f);
     PPBR_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("dominator_Hull", 1f);
     C.put("venture_Hull", 3f);
     PPBR_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("conquest_Hull", 1f);
     PPBR_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("piranha_wing", 1f);
     W.put("talon_wing", 1.5f);
     W.put("brdy_krait_wing", 0.375f);
     W.put("brdy_squilla_wing", 0.375f);
     W.put("brdy_serket_wing", 0.125f);
     PPBR_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("condor_Hull", 2f);
     CRR.put("venture_Hull", 1f);
     CRR.put("brdy_typheus_Hull", 1f);
     PPBR_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("mule_Hull", 2f);
     TR.put("venture_Hull", 1f);
     TR.put("tarsus_Hull", 1f);
     TR.put("buffalo2_Hull", 2f);
     TR.put("brdy_cetonia_Hull", 1f);
     PPBR_TR = Collections.unmodifiableMap(TR);
     } 
     
     static 
     {
     Map F = new HashMap();
     F.put("shade_Hull", 2f);
     F.put("afflictor_Hull", 2f);
     F.put("omen_Hull", 3f);
     F.put("tempest_Hull", 3f);
     F.put("hyperion_Hull", 1f);
     F.put("wolf_Hull", 2f);
     TT_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("medusa_Hull", 2f);
     D.put("sunder_Hull", 1f);
     TT_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("aurora_Hull", 2f);
     C.put("doom_Hull", 1f);
     TT_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("astral_Hull", 1f);
     CS.put("paragon_Hull", 1f);
     CS.put("odyssey_Hull", 1f);
     TT_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("longbow_wing", 0.5f);
     W.put("trident_wing", 0.33f);
     W.put("dagger_wing", 0.33f);
     W.put("wasp_wing", 1f);
     W.put("xyphos_wing", 0.5f);
     TT_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("astral_Hull", 1f);
     TT_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("buffalo_Hull", 1f);
     TT_TR = Collections.unmodifiableMap(TR);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("brawler_Hull", 2f);
     F.put("vigilance_Hull", 2f);
     F.put("lasher_Hull", 1f);
     F.put("wolf_Hull", 1f);
     SIN_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("medusa_Hull", 1f);
     D.put("sunder_Hull", 1f);
     D.put("hammerhead_Hull", 2f);
     D.put("gemini_Hull", 1.5f);
     SIN_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("eagle_Hull", 2f);
     C.put("falcon_Hull", 2f);
     C.put("apogee_Hull", 1f);
     SIN_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("conquest_Hull", 1f);
     SIN_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("gladius_wing", 1f);
     W.put("warthog_wing", 1f);
     W.put("thunder_wing", 1f);
     SIN_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("gemini_Hull", 1f);
     SIN_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("valkyrie_Hull", 1f);
     TR.put("dram_Hull", 2f);
     SIN_TR = Collections.unmodifiableMap(TR);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("brawler_Hull", 1f);
     F.put("vigilance_Hull", 1f);
     F.put("lasher_Hull", 1f);
     F.put("wolf_Hull", 1f);
     F.put("hound_Hull", 1f);
     F.put("shade_Hull", 0.33f);
     F.put("afflictor_Hull", 0.33f);
     F.put("syndicate_asp_diamondback_Hull", 1f);
     IND_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("medusa_Hull", 1f);
     D.put("sunder_Hull", 1f);
     D.put("hammerhead_Hull", 2f);
     D.put("gemini_Hull", 2f);
     D.put("mule_Hull", 2f);
     D.put("syndicate_asp_copperhead_Hull", 2f);
     IND_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("eagle_Hull", 2f);
     C.put("falcon_Hull", 2f);
     C.put("apogee_Hull", 1f);
     C.put("doom_Hull", 1f);
     C.put("aurora_Hull", 1f);
     C.put("dominator_Hull", 1f);
     C.put("venture_Hull", 1f);
     C.put("syndicate_asp_gigantophis_Hull", 2f);
     IND_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("conquest_Hull", 1f);
     CS.put("odyssey_Hull", 1f);
     IND_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("gladius_wing", 2f);
     W.put("warthog_wing", 2f);
     W.put("thunder_wing", 2f);
     W.put("xyphos_wing", 0.5f);
     W.put("talon_wing", 1f);
     W.put("wasp_wing", 0.5f);
     W.put("syndicate_asp_bite_wing", 1f);
     W.put("syndicate_asp_venom_wing", 1f);
     W.put("syndicate_asp_constrictor_wing", 1f);
     IND_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("gemini_Hull", 2f);
     CRR.put("condor_Hull", 1f);
     CRR.put("venture_Hull", 1f);
     IND_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("valkyrie_Hull", 1f);
     TR.put("dram_Hull", 1f);
     TR.put("buffalo_Hull", 0.5f);
     TR.put("mule_Hull", 1f);
     TR.put("venture_Hull", 1f);
     TR.put("tarsus_Hull", 1f);
     TR.put("phaeton_Hull", 0.5f);
     TR.put("shuttle_Hull", 1f);
     TR.put("gemini_Hull", 1f);
     TR.put("syndicate_asp_gigantophis_Hull", 1f);
     TR.put("syndicate_asp_copperhead_Hull", 1f);
     TR.put("syndicate_asp_diamondback_Hull", 1f);
     IND_TR = Collections.unmodifiableMap(TR);
     }
     
     static 
     {
     Map LG = new HashMap();
     LG.put("crig_Standard", 1f);
     LG.put("ox_Standard", 2f);
     CIV_LG = Collections.unmodifiableMap(LG);
     
     Map W = new HashMap();
     W.put("mining_drone_wing", 1f);
     CIV_W= Collections.unmodifiableMap(W);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("pack_wirefox_Hull", 1f);
     F.put("pack_bedlington_Hull", 0.75f);
     PCK_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("pack_ridgeback_Hull", 0.5f);
     D.put("pack_ridgeback_x_Hull", 0.75f);
     D.put("pack_pitbull_Hull", 0.5f);
     D.put("pack_BRT_Hull", 1f);
     D.put("pack_komondor_Hull", 1f);
     PCK_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     PCK_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     PCK_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     PCK_W= Collections.unmodifiableMap(W);
     
     Map TR = new HashMap();
     TR.put("pack_samoyed_Hull", 1f);
     TR.put("pack_samoyed_decoupled_Hull", 0.5f);
     PCK_TR = Collections.unmodifiableMap(TR);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("junk_pirates_clam_Hull", 1f);
     F.put("junk_pirates_hammer_Hull", 1f);
     F.put("junk_pirates_sickle_Hull", 1f);
     JP_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("junk_pirates_octopus_Hull", 1f);
     D.put("junk_pirates_boxer_Hull", 1f);
     D.put("junk_pirates_boxenstein_Hull", 1f);
     D.put("junk_pirates_scythe_Hull", 1f);
     D.put("junk_pirates_langoustine_Hull", 1f);
     D.put("junk_pirates_turbot_Hull", 0.5f);
     JP_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("junk_pirates_dugong_Hull", 1f);
     C.put("junk_pirates_goat_Hull", 1f);
     C.put("junk_pirates_orca_Hull", 1f);
     JP_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("junk_pirates_kraken_Hull", 1f);
     JP_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("junk_pirates_cleat_wing", 1f);
     W.put("junk_pirates_spike_wing", 1f);
     W.put("junk_pirates_shard_wing", 1f);
     W.put("junk_pirates_splinter_wing", 1f);
     JP_W= Collections.unmodifiableMap(W);
     
     Map U = new HashMap();
     U.put("junk_pirates_the_reaper_Hull", 1f);
     JP_U = Collections.unmodifiableMap(U);
     
     Map TR = new HashMap();
     TR.put("junk_pirates_stoatB_Hull", 1f);
     TR.put("junk_pirates_stoatA_Hull", 1f);
     JP_TR = Collections.unmodifiableMap(TR);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("ms_seski_Hull", 1f);
     F.put("ms_enlil_Hull", 2f);
     F.put("ms_shamash_Hull", 0.5f);
     F.put("ms_inanna_Hull", 1f);
     SHI_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("ms_morningstar_Hull", 2f);
     D.put("ms_sargasso_Hull", 1f);
     SHI_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("ms_charybdis_Hull", 2f);
     C.put("ms_elysium_Hull", 1f);
     C.put("ms_tartarus_Hull", 1f);
     C.put("ms_scylla_Hull", 1f);
     SHI_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("ms_mimir_Hull", 1f);
     SHI_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("ms_skinwalker_wing", 1f);
     W.put("ms_neriad_wing", 2f);
     W.put("ms_raksasha_wing", 1f);
     SHI_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("ms_sargasso_Hull", 1f);
     SHI_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("ms_lambent_Hull", 1f);
     TR.put("ms_solidarity_Hull", 1f);
     SHI_TR = Collections.unmodifiableMap(TR);
     
     Map U = new HashMap();
     U.put("ms_mimirBaus_Hull", 1f);
     SHI_U = Collections.unmodifiableMap(U);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("brdy_scarab_Hull", 2f);
     F.put("brdy_mantis_Hull", 1f);
     F.put("brdy_locust_Hull", 2f);
     F.put("brdy_robberfly_Hull", 2f);
     BR_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("brdy_cetonia_Hull", 0.5f);
     D.put("brdy_desdinova_Hull", 2f);
     D.put("brdy_gonodactylus_Hull", 1f);
     D.put("brdy_typheus_Hull", 1f);
     BR_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     C.put("brdy_stenos_Hull", 1f);
     C.put("brdy_revenant_Hull", 1f);
     C.put("brdy_nevermore_Hull", 2f);
     BR_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("brdy_kurmaraja_Hull", 1f);
     CS.put("brdy_karkinos_Hull", 1f);
     BR_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("brdy_krait_wing", 2f);
     W.put("brdy_squilla_wing", 2f);
     W.put("brdy_serket_wing", 1f);
     BR_W= Collections.unmodifiableMap(W);
     
     Map CRR = new HashMap();
     CRR.put("brdy_typheus_Hull", 2f);
     CRR.put("brdy_revenant_Hull", 1f);
     BR_CRR = Collections.unmodifiableMap(CRR);
     
     Map TR = new HashMap();
     TR.put("brdy_cetonia_Hull", 1f);
     BR_TR = Collections.unmodifiableMap(TR);
     
     Map U = new HashMap();
     U.put("nevermoreB_Hull", 1f);
     U.put("brdy_stormcrow_Hull", 1f);
     BR_U = Collections.unmodifiableMap(U);
     }
     
     static 
     {
     Map F = new HashMap();
     F.put("nom_wurm_assault", 2f);
     F.put("nom_yellowjacket_sniper", 1f);
     F.put("nom_death_bloom_strike", 0.5f);
     NOM_F = Collections.unmodifiableMap(F);
     
     Map D = new HashMap();
     D.put("nom_scorpion_assault", 1f);
     D.put("nom_komodo_mk2_assault", 0.5f);
     D.put("nom_komodo_assault", 2f);
     D.put("nom_flycatcher_carrier", 1f);
     NOM_D = Collections.unmodifiableMap(D);
     
     Map C = new HashMap();
     NOM_C = Collections.unmodifiableMap(C);
     
     Map CS = new HashMap();
     CS.put("nom_sandstorm_assault", 2f);
     CS.put("nom_gila_monster_antibattleship", 1f);
     NOM_CS = Collections.unmodifiableMap(CS);
     
     Map W = new HashMap();
     W.put("nom_iguana_wing", 1f);
     W.put("nom_scarab_wing", 1f);
     W.put("nom_toad_wing", 1f);
     NOM_W = Collections.unmodifiableMap(W);
     }
     
     static
     {
         Map W_map = new HashMap();
         W_map.putAll(HE_W);
         W_map.putAll(IND_W);
         W_map.putAll(TT_W);
         W_map.putAll(PCK_W);
         W_map.putAll(JP_W);
         W_map.putAll(SHI_W);
         W_map.putAll(BR_W);  
         W_map.putAll(NOM_W);
         ALL_W_FINAL = Collections.unmodifiableMap(W_map);
         
         Map F_map = new HashMap();
         F_map.putAll(HE_F);
         F_map.putAll(IND_F);
         F_map.putAll(TT_F);
         F_map.putAll(PCK_F);
         F_map.putAll(JP_F);
         F_map.putAll(SHI_F);
         F_map.putAll(BR_F);  
         F_map.putAll(NOM_F); 
         ALL_F_FINAL = Collections.unmodifiableMap(F_map);
         
         Map D_map = new HashMap();
         D_map.putAll(HE_D);
         D_map.putAll(IND_D);
         D_map.putAll(TT_D);
         D_map.putAll(PCK_D);
         D_map.putAll(JP_D);
         D_map.putAll(SHI_D);
         D_map.putAll(BR_D);   
         D_map.putAll(NOM_D);
         ALL_D_FINAL = Collections.unmodifiableMap(D_map);
         
         Map C_map = new HashMap();
         C_map.putAll(HE_C);
         C_map.putAll(IND_C);
         C_map.putAll(TT_C);
         C_map.putAll(PCK_C);
         C_map.putAll(JP_C);
         C_map.putAll(SHI_C);
         C_map.putAll(BR_C);       
         C_map.putAll(NOM_C);
         ALL_C_FINAL = Collections.unmodifiableMap(C_map);
         
         Map CS_map = new HashMap();
         CS_map.putAll(HE_CS);
         CS_map.putAll(IND_CS);
         CS_map.putAll(TT_CS);
         CS_map.putAll(PCK_CS);
         CS_map.putAll(JP_CS);
         CS_map.putAll(SHI_CS);
         CS_map.putAll(BR_CS);     
         CS_map.putAll(NOM_CS); 
         ALL_CS_FINAL = Collections.unmodifiableMap(CS_map);
     }
     
 //    public static final String [] IF_F = {"scythe_Hull","rickshaw_Hull","echo_Hull","dakota_Hull","albatross_Hull"};
 //    public static final String [] IF_D = {"defiant_Hull","shogun_Hull","toa_Hull","vixen_Hull","zephyr_Hull"};
 //    public static final String [] IF_C = {"ares_Hull","ballista_Hull","helios_Hull","mercury_Hull","montana_Hull"};
 //    public static final String [] IF_CS = {"antares_Hull","auria_Hull","titan_Hull","yukon_Hull"};
 //    public static final String [] IF_W = {"hornet_wing","draken_wing","tracer_wing","foxbat_wing"};
     
 //    public static final String [] GD_F = {"gedune_tychrea_Hull","gedune_nanda_Hull","gedune_kyirus_Hull","gedune_kitsune_Hull"};
 //    public static final String [] GD_D = {"gedune_tenzen_Hull","gedune_byakuri_Hull","gedune_dahaki_Hull"};
 //    public static final String [] GD_C = {"gedune_bakoros_Hull"};
 //    public static final String [] GD_CS = {};
 //    public static final String [] GD_W = {"gedune_chua_wing","gedune_duri_wing","gedune_viper_wing"};
     
 //    public static final String [] NC_F = {"neutrino_causality_Hull","neutrino_relativity_Hull","neutrino_singularity_Hull"};
 //    public static final String [] NC_D = {"neutrino_piledriver_Hull","neutrino_vice_Hull","neutrino_hacksaw_Hull","neutrino_sledgehammer_Hull"};
 //    public static final String [] NC_C = {"neutrino_lathe_Hull","neutrino_grinder_Hull","neutrino_nirvash_Hull","neutrino_theend_Hull"};
 //    public static final String [] NC_CS = {"neutrino_hildolfr_Hull","neutrino_jackhammer_Hull","neutrino_colossus_Hull","neutrino_banshee_Hull"}; //"neutrino_unsung_Hull",
 //    public static final String [] NC_W = {"neutrino_drohne_wing","neutrino_schwarzgeist_wing","neutrino_drache_wing","neutrino_floh_wing","neutrino_schwarm_wing"};
     
 //    public static final String [] TL_F = {"thule_vikingmkii_Hull","thule_vikingmki_Hull"};
 //    public static final String [] TL_D = {"thule_berserker_Hull","thule_tungsten_Hull"};
 //    public static final String [] TL_C = {"thule_ragnarok_Hull","thule_solidstorm_Hull","thule_vanir_Hull"};
 //    public static final String [] TL_CS = {"thule_oberon_Hull","thule_herzog_Hull"};  
 //    public static final String [] TL_W = {"thule_wartool_wing","thule_einherjer_wing","thule_komet_wing","thule_gunnr_wing"};
 
 //static
 //    {
 //        Map allVariantsTemp = new HashMap();
 //
 //        allVariantsTemp.put("titan", createVariantList(new String[]{"titan_Siege"}));
 //        allVariantsTemp.put("auria",createVariantList(new String[]{"auria_Standard"}));
 //        allVariantsTemp.put("antares",createVariantList(new String[]{"antares_Assault"}));
 //        allVariantsTemp.put("yukon",createVariantList(new String[]{"yukon_Standard"}));
 //        allVariantsTemp.put("ballista",createVariantList(new String[]{"ballista_Standard"}));
 //        allVariantsTemp.put("mercury",createVariantList(new String[]{"mercury_Standard"}));
 //        allVariantsTemp.put("ares",createVariantList(new String[]{"ares_Standard"}));
 //        allVariantsTemp.put("montana",createVariantList(new String[]{"montana_Utility"}));
 //        allVariantsTemp.put("helios",createVariantList(new String[]{"helios_Assault"}));
 //        allVariantsTemp.put("toa",createVariantList(new String[]{"toa_Assault", "toa_Balanced"}));
 //        allVariantsTemp.put("defiant",createVariantList(new String[]{"defiant_Assault"}));
 //        allVariantsTemp.put("vixen",createVariantList(new String[]{"vixen_Assault", "vixen_Standard"}));
 //        allVariantsTemp.put("shogun",createVariantList(new String[]{"shogun_Assault", "shogun_Support"}));
 //        allVariantsTemp.put("zephyr",createVariantList(new String[]{"zephyr_Strike"}));
 //        allVariantsTemp.put("scythe",createVariantList(new String[]{"scythe_Fighter", "scythe_Frigate"}));
 //        allVariantsTemp.put("dakota",createVariantList(new String[]{"dakota_Standard"}));
 //        allVariantsTemp.put("rickshaw",createVariantList(new String[]{"rickshaw_Standard", "rickshaw_Strike"}));
 //        allVariantsTemp.put("echo",createVariantList(new String[]{"echo_Standard"}));
 //        allVariantsTemp.put("albatross",createVariantList(new String[]{"albatross_Attack"}));
 //        allVariantsTemp.put("mazerk",createVariantList(new String[]{"mazerk_Standard"}));
 //        allVariantsTemp.put("orion",createVariantList(new String[]{"orion_Standard"}));
 //        allVariantsTemp.put("tahoe",createVariantList(new String[]{"tahoe_Standard"}));
 //        allVariantsTemp.put("trexel",createVariantList(new String[]{"trexel_Standard"}));
 //        Variants_IF = Collections.unmodifiableMap(allVariantsTemp);
 //    }
     
     static
     {
         Map allVariantsTemp = new HashMap();        
         allVariantsTemp.put("brdy_cetonia",createVariantList(new String[]{"brdy_cetonia_standard"}));
         allVariantsTemp.put("brdy_karkinos",createVariantList(new String[]{"brdy_karkinos_assault", "brdy_karkinos_prototype"}));
         allVariantsTemp.put("brdy_kurmaraja",createVariantList(new String[]{"brdy_kurmaraja_elite"}));
 	allVariantsTemp.put("brdy_locust",createVariantList(new String[]{"brdy_locust_patrol", "brdy_locust_strike", "brdy_locust_wing"}));
 	allVariantsTemp.put("brdy_mantis",createVariantList(new String[]{"brdy_mantis_elite", "brdy_mantis_attack", "brdy_mantis_strike"}));
 	allVariantsTemp.put("brdy_revenant",createVariantList(new String[]{"brdy_revenant_carrier"}));
 	allVariantsTemp.put("brdy_robberfly",createVariantList(new String[]{"brdy_robberfly_cs", "brdy_robberfly_light", "brdy_robberfly_strike"}));
         allVariantsTemp.put("brdy_stenos",createVariantList(new String[]{"brdy_stenos_exploration"}));	
         allVariantsTemp.put("brdy_stormcrow",createVariantList(new String[]{"brdy_stormcrow_cyc"}));
         allVariantsTemp.put("brdy_typheus",createVariantList(new String[]{"brdy_typheus_elite", "brdy_typheus_support"}));
         allVariantsTemp.put("brdy_gonodactylus",createVariantList(new String[]{"gonodactylus_assault", "gonodactylus_CS"}));
         allVariantsTemp.put("brdy_nevermore",createVariantList(new String[]{"nevermore_advanced", "nevermore_assault", "nevermore_tac"}));
         allVariantsTemp.put("nevermoreB",createVariantList(new String[]{"nevermoreB_prototype"}));
         allVariantsTemp.put("brdy_desdinova",createVariantList(new String[]{"desdinova_assault", "desdinova_cs", "desdinova_fastattack","desdinova_HK"}));
         allVariantsTemp.put("brdy_scarab",createVariantList(new String[]{"scarab_closesupport", "scarab_firesupport", "scarab_pd","scarab_strike","scarab_attack"}));
         Variants_BR = Collections.unmodifiableMap(allVariantsTemp);
     }
     
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("ms_charybdis",createVariantList(new String[]{"ms_charybdis_Attack", "ms_charybdis_Balanced", "ms_charybdis_CS", "ms_charybdis_PD", "ms_charybdis_Standard"}));
         allVariantsTemp.put("ms_elysium",createVariantList(new String[]{"ms_elysium_Assault", "ms_elysium_CS", "ms_elysium_PD", "ms_elysium_Standard", "ms_elysium_Strike"}));
         allVariantsTemp.put("ms_enlil",createVariantList(new String[]{"ms_enlil_AF", "ms_enlil_Attack", "ms_enlil_Balanced", "ms_enlil_CS", "ms_enlil_LRM", "ms_enlil_PD", "ms_enlil_Standard", "ms_enlil_Strike"}));
         allVariantsTemp.put("ms_inanna",createVariantList(new String[]{"ms_inanna_Assault", "ms_inanna_CS", "ms_inanna_EMP", "ms_inanna_Standard", "ms_inanna_Strike"}));
         allVariantsTemp.put("ms_lambent",createVariantList(new String[]{"ms_lambent_Standard"}));
         allVariantsTemp.put("ms_mimir",createVariantList(new String[]{"ms_mimir_Assault", "ms_mimir_CS", "ms_mimir_PD", "ms_mimir_Standard"}));
         allVariantsTemp.put("ms_mimirBaus",createVariantList(new String[]{"ms_mimirBaus_Baus"}));
         allVariantsTemp.put("ms_morningstar",createVariantList(new String[]{"ms_morningstar_AF", "ms_morningstar_Assault", "ms_morningstar_CS", "ms_morningstar_PD", "ms_morningstar_Standard", "ms_morningstar_Strike"}));
         allVariantsTemp.put("ms_sargasso",createVariantList(new String[]{"ms_sargasso_Assault", "ms_sargasso_Balanced", "ms_sargasso_EMP", "ms_sargasso_LRM", "ms_sargasso_Standard"}));
         allVariantsTemp.put("ms_seski",createVariantList(new String[]{"ms_seski_Attack", "ms_seski_BR", "ms_seski_CS", "ms_seski_Standard"}));
         allVariantsTemp.put("ms_shamash",createVariantList(new String[]{"ms_shamash_Attack", "ms_shamash_CS", "ms_shamash_EMP", "ms_shamash_Standard"}));
         allVariantsTemp.put("ms_tartarus",createVariantList(new String[]{"ms_tartarus_AF", "ms_tartarus_Assault", "ms_tartarus_CS", "ms_tartarus_Standard"}));
         allVariantsTemp.put("ms_scylla",createVariantList(new String[]{"ms_scylla_Assault", "ms_scylla_Beam", "ms_scylla_Standard"}));
         allVariantsTemp.put("ms_solidarity",createVariantList(new String[]{"ms_solidarity_Standard","ms_solidarity_Fast"}));
         Variants_SHI = Collections.unmodifiableMap(allVariantsTemp);
     }
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("atlas",createVariantList(new String[]{"atlas_Standard"}));
         allVariantsTemp.put("dominator",createVariantList(new String[]{"dominator_ass", "dominator_pun", "dominator_sho", "dominator_sni", "dominator_sto","dominatorhe_sup", "dominatorhe_ult"}));
         allVariantsTemp.put("dram",createVariantList(new String[]{"dram_ass", "dram_lig", "dram_sni", "dram_sto", "dram_sup"}));
 	allVariantsTemp.put("enforcer",createVariantList(new String[]{"enforcer_ass", "enforcer_out", "enforcer_sho", "enforcer_sni", "enforcer_str","enforcerhe_cru", "enforcerhe_tor"}));
 	allVariantsTemp.put("hound",createVariantList(new String[]{"hound_ass", "hound_hvs", "hound_rai", "hound_sni", "hound_sto"}));
 	allVariantsTemp.put("lasher",createVariantList(new String[]{"lasher_bul", "lasher_hun", "lasher_nee", "lasher_pd", "lasher_pun", "lasher_sup", "lasher_trr"}));
 	allVariantsTemp.put("mule",createVariantList(new String[]{"mulep_ass", "mulep_eli", "mulep_out", "mulep_sta", "mulep_sup"}));
         allVariantsTemp.put("onslaught",createVariantList(new String[]{"onslaught_bul", "onslaught_eli", "onslaught_out", "onslaught_pun", "onslaught_sni", "onslaught_sta", "onslaught_sto","onslaughthe_hur", "onslaughthe_tsu"}));	
         allVariantsTemp.put("phaeton",createVariantList(new String[]{"phaetoni_har", "phaetoni_sni", "phaetoni_sta", "phaetoni_sto", "phaetoni_str"}));
         allVariantsTemp.put("condor",createVariantList(new String[]{"condor_ass", "condor_out", "condor_sni", "condor_sto", "condor_str"}));
         allVariantsTemp.put("tarsus",createVariantList(new String[]{"tarsusi_ass", "tarsusi_com", "tarsusi_sni", "tarsusi_sto", "tarsusi_sup"}));
         allVariantsTemp.put("valkyrie",createVariantList(new String[]{"valkyriei_sto", "valkyriei_def", "valkyriei_eli", "valkyriei_run", "valkyriei_sni"}));
         Variants_HEG = Collections.unmodifiableMap(allVariantsTemp);
     }
         
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("phaeton",createVariantList(new String[]{"phaetoni_har", "phaetoni_sni", "phaetoni_sta", "phaetoni_sto", "phaetoni_str"}));
         allVariantsTemp.put("atlas",createVariantList(new String[]{"atlas_Standard"}));
         allVariantsTemp.put("doom",createVariantList(new String[]{"doomi_ass", "doomi_bul", "doomi_pun", "doomi_str"}));
         allVariantsTemp.put("dram",createVariantList(new String[]{"drami_ass", "drami_out", "drami_pd", "drami_pun", "drami_sup"}));
         allVariantsTemp.put("eagle",createVariantList(new String[]{"eaglei_ass", "eaglei_bal", "eaglei_pul", "eaglei_pun", "eaglei_sni", "eaglei_sto", "eaglei_str"}));
         allVariantsTemp.put("falcon",createVariantList(new String[]{"falconi_att", "falconi_bal", "falconi_cs", "falconi_out", "falconi_pun", "falconi_sni", "falconi_sta", "falconi_sto"}));
         allVariantsTemp.put("gemini",createVariantList(new String[]{"geminii_cla", "geminii_cs", "geminii_out", "geminii_sni", "geminii_sta", "geminii_sto", "geminii_str", "geminii_sup"}));
         allVariantsTemp.put("hammerhead",createVariantList(new String[]{"hammerheadi_bal", "hammerheadi_bul", "hammerheadi_eli", "hammerheadi_out", "hammerheadi_pun", "hammerheadi_sni", "hammerheadi_str","hammerheadie_sup", "hammerheadie_twi"}));
         allVariantsTemp.put("hound",createVariantList(new String[]{"houndi_ass", "houndi_hes", "houndi_mac", "houndi_str"}));
         allVariantsTemp.put("lasher",createVariantList(new String[]{"lasheri_def", "lasheri_pus", "lasheri_str"}));
         allVariantsTemp.put("medusa",createVariantList(new String[]{"medusai_att", "medusai_cs", "medusai_pd", "medusai_sho", "medusai_str"}));	
         allVariantsTemp.put("odyssey",createVariantList(new String[]{"odysseyi_bea", "odysseyi_sho", "odysseyi_str"}));	
         allVariantsTemp.put("mule",createVariantList(new String[]{"mulei_ass", "mulei_bur", "mulei_sho", "mulei_sni", "mulei_sto"}));
         allVariantsTemp.put("shade",createVariantList(new String[]{"shadei_ass", "shadei_hun", "shadei_sho", "shadei_sni", "shadei_sto"}));
         allVariantsTemp.put("shuttle",createVariantList(new String[]{"shuttlei_ass", "shuttlei_att", "shuttlei_bur", "shuttlei_pd", "shuttlei_sni", "shuttlei_sta", "shuttlei_sto", "shuttlei_sup"}));
         allVariantsTemp.put("sunder",createVariantList(new String[]{"sunderi_ass", "vbul", "sunderi_cs", "sunderi_hun", "sunderi_pla", "sunderi_pul", "sunderi_str"}));
         allVariantsTemp.put("tarsus",createVariantList(new String[]{"tarsusi_ass", "tarsusi_com", "tarsusi_sni", "tarsusi_sto", "tarsusi_sup"}));
         allVariantsTemp.put("afflictor",createVariantList(new String[]{"afflictori_ass", "afflictori_hun", "afflictori_str"}));
         allVariantsTemp.put("apogee",createVariantList(new String[]{"apogeei_ass", "apogeei_bal", "apogeei_cru", "apogeei_pun", "apogeei_str"}));
         allVariantsTemp.put("aurora",createVariantList(new String[]{"aurorai_ass", "aurorai_att", "aurorai_str"}));
         allVariantsTemp.put("brawler",createVariantList(new String[]{"brawleri_ass", "brawleri_bul", "brawleri_old", "brawleri_out", "brawleri_pun", "brawleri_sni", "brawleri_sto"}));
         allVariantsTemp.put("buffalo",createVariantList(new String[]{"buffaloi_ass", "buffaloi_pd", "buffaloi_sta", "buffaloi_str", "buffaloi_sup"}));//TT
         allVariantsTemp.put("conquest",createVariantList(new String[]{"conquesti_ass", "conquesti_bul", "conquesti_eli", "conquesti_out", "conquesti_sho", "conquesti_sni", "conquesti_sto"}));
         allVariantsTemp.put("dominator",createVariantList(new String[]{"dominatori_ass", "dominatori_sho", "dominatori_sni", "dominatori_str"}));
         allVariantsTemp.put("wolf",createVariantList(new String[]{"wolfi_ass", "wolfi_cs", "wolfi_pd", "wolfi_pom", "wolfi_str"}));
         allVariantsTemp.put("vigilance",createVariantList(new String[]{"vigilancei_ass", "vigilancei_fis", "vigilancei_hun", "vigilancei_inh", "vigilancei_pat", "vigilancei_str", "vigilancei_sup"}));
         allVariantsTemp.put("venture",createVariantList(new String[]{"venturei_pd", "venturei_pun", "venturei_sni", "venturei_sto", "venturei_sup"}));
         allVariantsTemp.put("valkyrie",createVariantList(new String[]{"valkyriei_sto", "valkyriei_def", "valkyriei_eli", "valkyriei_run", "valkyriei_sni"}));
         Variants_IND = Collections.unmodifiableMap(allVariantsTemp);
     }
         
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("brdy_cetonia",createVariantList(new String[]{"brdy_cetonia_standard"}));
         allVariantsTemp.put("brdy_locust",createVariantList(new String[]{"brdy_locust_patrol", "brdy_locust_strike", "brdy_locust_wing"}));
 	allVariantsTemp.put("brdy_mantis",createVariantList(new String[]{"brdy_mantis_elite", "brdy_mantis_attack", "brdy_mantis_strike"}));
         allVariantsTemp.put("brdy_robberfly",createVariantList(new String[]{"brdy_robberfly_cs", "brdy_robberfly_light", "brdy_robberfly_strike"}));
         allVariantsTemp.put("brdy_typheus",createVariantList(new String[]{"brdy_typheus_elite", "brdy_typheus_support"}));
         allVariantsTemp.put("brdy_gonodactylus",createVariantList(new String[]{"gonodactylus_assault", "gonodactylus_CS"}));
         allVariantsTemp.put("brdy_desdinova",createVariantList(new String[]{"desdinova_assault", "desdinova_cs", "desdinova_fastattack","desdinova_HK"}));
         allVariantsTemp.put("brdy_scarab",createVariantList(new String[]{"scarab_closesupport", "scarab_firesupport", "scarab_pd","scarab_strike","scarab_attack"}));
         allVariantsTemp.put("conquest",createVariantList(new String[]{"conquesti_ass", "conquesti_bul", "conquesti_eli", "conquesti_out", "conquesti_sho", "conquesti_sni", "conquesti_sto"}));
         allVariantsTemp.put("condor",createVariantList(new String[]{"condorp_ass", "condorp_fis", "condorp_pd", "condorp_str", "condorp_sup"}));
         allVariantsTemp.put("buffalo2",createVariantList(new String[]{"buffalo2p_ass", "buffalo2p_har", "buffalo2p_spa", "buffalo2p_str", "buffalo2p_sup"}));
         allVariantsTemp.put("enforcer",createVariantList(new String[]{"enforcerp_ass", "enforcerp_bal", "enforcerp_cs", "enforcerp_eli", "enforcerp_hun", "enforcerp_out", "enforcerp_str","enforcerpe_blb", "enforcerpe_heh"}));
         allVariantsTemp.put("hound",createVariantList(new String[]{"houndp_ass", "houndp_out", "houndp_rac", "houndp_str"}));
         allVariantsTemp.put("lasher",createVariantList(new String[]{"lasherp_ass", "lasherp_ast", "lasherp_cls", "lasherp_sal", "lasherp_sho", "lasherp_sto", "lasherp_str"}));
         allVariantsTemp.put("mule",createVariantList(new String[]{"mulep_ass", "mulep_eli", "mulep_out", "mulep_sta", "mulep_sup"}));
         allVariantsTemp.put("tarsus",createVariantList(new String[]{"tarsusp_ass", "tarsusp_out", "tarsusp_pd", "tarsusp_sta", "tarsusp_str"}));
         allVariantsTemp.put("dominator",createVariantList(new String[]{"dominatorp_ass", "dominatorp_che", "dominatorp_hun", "dominatorp_out", "dominatorp_sho", "dominatorp_str", "dominatorp_sup","dominatorpe_dev", "dominatorpe_rin"}));
         allVariantsTemp.put("venture",createVariantList(new String[]{"venturep_ass", "venturep_bal", "venturep_out", "venturep_pun", "venturep_sup"}));
         allVariantsTemp.put("ms_enlil",createVariantList(new String[]{"ms_enlil_AF", "ms_enlil_Attack", "ms_enlil_Balanced", "ms_enlil_CS", "ms_enlil_LRM", "ms_enlil_PD", "ms_enlil_Standard", "ms_enlil_Strike"}));
         allVariantsTemp.put("ms_inanna",createVariantList(new String[]{"ms_inanna_Assault", "ms_inanna_CS", "ms_inanna_EMP", "ms_inanna_Standard", "ms_inanna_Strike"}));
         allVariantsTemp.put("ms_lambent",createVariantList(new String[]{"ms_lambent_Standard"}));
         allVariantsTemp.put("ms_morningstar",createVariantList(new String[]{"ms_morningstar_AF", "ms_morningstar_Assault", "ms_morningstar_CS", "ms_morningstar_PD", "ms_morningstar_Standard", "ms_morningstar_Strike"}));
         allVariantsTemp.put("ms_sargasso",createVariantList(new String[]{"ms_sargasso_Assault", "ms_sargasso_Balanced", "ms_sargasso_EMP", "ms_sargasso_LRM", "ms_sargasso_Standard"}));
         allVariantsTemp.put("ms_seski",createVariantList(new String[]{"ms_seski_Attack", "ms_seski_BR", "ms_seski_CS", "ms_seski_Standard"}));
         allVariantsTemp.put("ms_shamash",createVariantList(new String[]{"ms_shamash_Attack", "ms_shamash_CS", "ms_shamash_EMP", "ms_shamash_Standard"}));
         allVariantsTemp.put("ms_solidarity",createVariantList(new String[]{"ms_solidarity_Standard"}));
         Variants_PIR = Collections.unmodifiableMap(allVariantsTemp);
     }
         
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("odyssey",createVariantList(new String[]{"odyssey_ass", "odyssey_bul", "odyssey_sni", "odyssey_str","odysseyte_eli", "odysseyte_ult"}));
         allVariantsTemp.put("omen",createVariantList(new String[]{"omen_att", "omen_hun", "omen_pd", "omen_sho", "omen_str"}));
         allVariantsTemp.put("tempest",createVariantList(new String[]{"tempest_att", "tempest_bea", "tempest_hun", "tempest_pd", "tempest_pun", "tempest_sho", "tempest_str"}));
         allVariantsTemp.put("afflictor",createVariantList(new String[]{"afflictor_bea", "afflictor_hun", "afflictor_out", "afflictor_sni", "afflictor_sto", "afflictor_str", "afflictor_sup"}));
         allVariantsTemp.put("astral",createVariantList(new String[]{"astral_att", "astral_eli", "astral_pd"}));
         allVariantsTemp.put("wolf",createVariantList(new String[]{"wolf_ass", "wolf_bea", "wolf_bul", "wolf_pun", "wolf_str"}));
         allVariantsTemp.put("shade",createVariantList(new String[]{"shade_pd", "shade_pun", "shade_str", "shade_sup", "shade_swa"}));
         allVariantsTemp.put("paragon",createVariantList(new String[]{"paragon_ass", "paragon_bul", "paragon_eli", "paragon_hun", "paragon_str","paragonte_sup", "paragonte_ult"}));
         allVariantsTemp.put("medusa",createVariantList(new String[]{"medusa_ass", "medusa_att", "medusa_bul", "medusa_sho", "medusa_str", "medusate_eli", "medusate_sup"}));
         allVariantsTemp.put("hyperion",createVariantList(new String[]{"hyperion_att", "hyperion_hun", "hyperion_out", "hyperion_pun", "hyperion_str"}));
         allVariantsTemp.put("doom",createVariantList(new String[]{"doom_ass", "doom_hun", "doom_sho", "doom_str"}));
         allVariantsTemp.put("aurora",createVariantList(new String[]{"aurora_ass", "aurora_att", "aurora_bal", "aurora_sta", "aurora_str"}));
         allVariantsTemp.put("buffalo",createVariantList(new String[]{"buffaloi_ass", "buffaloi_pd", "buffaloi_sta", "buffaloi_str", "buffaloi_sup"}));
         Variants_TT = Collections.unmodifiableMap(allVariantsTemp);
     }
         
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("pack_bedlington",createVariantList(new String[]{"pack_bedlington_Standard"}));
         allVariantsTemp.put("pack_BRT",createVariantList(new String[]{"pack_BRT_Guard", "pack_BRT_Strike", "pack_BRT_Support"}));
         allVariantsTemp.put("pack_pitbull",createVariantList(new String[]{"pack_pitbull_Standard"}));
         allVariantsTemp.put("pack_ridgeback",createVariantList(new String[]{"pack_ridgeback_Standard"}));
         allVariantsTemp.put("pack_ridgeback_x",createVariantList(new String[]{"pack_ridgeback_x_Standard"}));
         allVariantsTemp.put("pack_wirefox",createVariantList(new String[]{"pack_wirefox_Assault", "pack_wirefox_Standard"}));
         allVariantsTemp.put("pack_komondor",createVariantList(new String[]{"pack_komondor_Standard", "pack_komondor_Strike", "pack_komondor_Support"}));
         allVariantsTemp.put("pack_samoyed_decoupled",createVariantList(new String[]{"pack_samoyed_decoupled_Standard"}));
         allVariantsTemp.put("pack_samoyed",createVariantList(new String[]{"pack_samoyed_Standard", "pack_samoyed_Support"}));
         Variants_PCK = Collections.unmodifiableMap(allVariantsTemp);
     }
         
         static
     {
         Map allVariantsTemp = new HashMap();
         allVariantsTemp.put("junk_pirates_sickle",createVariantList(new String[]{"junk_pirates_sickle_Pointdefense","junk_pirates_sickle_Standard","junk_pirates_sickle_Strike"}));
         allVariantsTemp.put("junk_pirates_hammer",createVariantList(new String[]{"junk_pirates_hammer_Assault", "junk_pirates_hammer_Strike"}));
         allVariantsTemp.put("junk_pirates_clam",createVariantList(new String[]{"junk_pirates_clam_CS","junk_pirates_clam_Standard"}));
         allVariantsTemp.put("junk_pirates_stoatB",createVariantList(new String[]{"junk_pirates_stoatB_Standard"}));
         allVariantsTemp.put("junk_pirates_stoatA",createVariantList(new String[]{"junk_pirates_stoatA_Standard"}));
         allVariantsTemp.put("junk_pirates_octopus",createVariantList(new String[]{"junk_pirates_octopus_Standard"}));
         allVariantsTemp.put("junk_pirates_boxenstein",createVariantList(new String[]{"junk_pirates_boxenstein_Slugger", "junk_pirates_boxenstein_Support"}));
         allVariantsTemp.put("junk_pirates_boxer",createVariantList(new String[]{"junk_pirates_boxer_Fighter", "junk_pirates_boxer_Standard"}));
         allVariantsTemp.put("junk_pirates_langoustine",createVariantList(new String[]{"junk_pirates_langoustine_CS", "junk_pirates_langoustine_Standard","junk_pirates_langoustine_Strike"}));
         allVariantsTemp.put("junk_pirates_scythe",createVariantList(new String[]{"junk_pirates_scythe_Assault", "junk_pirates_scythe_Standard"}));
         allVariantsTemp.put("junk_pirates_turbot",createVariantList(new String[]{"junk_pirates_turbot_Assault", "junk_pirates_turbot_Strike"}));
         allVariantsTemp.put("junk_pirates_dugong",createVariantList(new String[]{"junk_pirates_dugong_Standard", "junk_pirates_dugong_Support"}));
         allVariantsTemp.put("junk_pirates_orca",createVariantList(new String[]{"junk_pirates_orca_Assault", "junk_pirates_orca_Standard"}));
         allVariantsTemp.put("junk_pirates_goat",createVariantList(new String[]{"junk_pirates_goat_Standard", "junk_pirates_goat_CS"}));
         allVariantsTemp.put("junk_pirates_kraken",createVariantList(new String[]{"junk_pirates_kraken_CS", "junk_pirates_kraken_Standard"}));
         allVariantsTemp.put("junk_pirates_the_reaper",createVariantList(new String[]{"junk_pirates_the_reaper_Standard"}));
         Variants_JP = Collections.unmodifiableMap(allVariantsTemp);
     }        
 
         static
     {
             Map allVariantsTemp = new HashMap();
             allVariantsTemp.putAll(Variants_PIR);
             allVariantsTemp.putAll(Variants_IND);
             allVariantsTemp.putAll(Variants_HEG);
             allVariantsTemp.putAll(Variants_TT);
             allVariantsTemp.putAll(Variants_PCK);
             allVariantsTemp.putAll(Variants_SHI);
             allVariantsTemp.putAll(Variants_BR);
             allVariantsTemp.putAll(Variants_JP);
             All_Variants = Collections.unmodifiableMap(allVariantsTemp);
     }
         private static List createVariantList(String[] variants)
     {
         return Collections.unmodifiableList(Arrays.asList(variants));
     }
         
 }
 
 
 
 
 
 
 
 
 
