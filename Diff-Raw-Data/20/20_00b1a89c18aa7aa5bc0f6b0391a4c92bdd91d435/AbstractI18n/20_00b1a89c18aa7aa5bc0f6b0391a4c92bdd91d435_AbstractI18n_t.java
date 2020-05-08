 /**
  * Copyright (C) 2011 BonitaSoft S.A.
  * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.web.toolkit.client.common.i18n;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
 import org.bonitasoft.web.toolkit.client.common.texttemplate.TextTemplate;
 import org.bonitasoft.web.toolkit.client.data.item.IItem;
 
 /**
  * @author Séverin Moussel
  * 
  */
 public abstract class AbstractI18n {
 
     public static enum LOCALE {
         en,
         en_US,
         fr,
         fr_FR,
         es,
         es_ES,
         it,
         it_IT,
         de,
         de_DE,
         pt_BR,
         aa_DJ,
         aa_ER_SAAHO,
         aa_ER,
         aa_ET,
         aa,
         af_NA,
         af,
         af_ZA,
         ak_GH,
         ak,
         am_ET,
         am,
         ar_AE,
         ar_BH,
         ar_DZ,
         ar_EG,
         ar_IQ,
         ar_JO,
         ar_KW,
         ar_LB,
         ar_LY,
         ar_MA,
         ar_OM,
         ar_QA,
         ar_SA,
         ar_SD,
         ar_SY,
         ar_TN,
         ar,
         ar_YE,
         as_IN,
         as,
         az_AZ,
         az_Cyrl_AZ,
         az_Cyrl,
         az_Latn_AZ,
         az,
         be_BY,
         be,
         bg_BG,
         bg,
         bn_BD,
         bn_IN,
         bn,
         bs_BA,
         bs,
         byn_ER,
         byn,
         ca_ES,
         ca,
         cch_NG,
         cch,
         cop,
         cs_CZ,
         cs,
         cy_GB,
         cy,
         da_DK,
         da,
         de_AT,
         de_BE,
         de_CH,
         de_LI,
         de_LU,
         dv_MV,
         dv,
         dz_BT,
         dz,
         ee_GH,
         ee_TG,
         ee,
         el_CY,
         el_GR,
         el_POLYTON,
         el,
         en_AS,
         en_AU,
         en_BE,
         en_BW,
         en_BZ,
         en_CA,
         en_Dsrt_US,
         en_Dsrt,
         en_GB,
         en_GU,
         en_HK,
         en_IE,
         en_IN,
         en_JM,
         en_MH,
         en_MP,
         en_MT,
         en_NA,
         en_NZ,
         en_PH,
         en_PK,
         en_SG,
         en_Shaw,
         en_TT,
         en_UM,
         en_US_POSIX,
         en_VI,
         en_ZA,
         en_ZW,
         eo,
         es_AR,
         es_BO,
         es_CL,
         es_CO,
         es_CR,
         es_DO,
         es_EC,
         es_GT,
         es_HN,
         es_MX,
         es_NI,
         es_PA,
         es_PE,
         es_PR,
         es_PY,
         es_SV,
         es_US,
         es_UY,
         es_VE,
         et_EE,
         et,
         eu_ES,
         eu,
         fa_AF,
         fa_IR,
         fa,
         fi_FI,
         fil_PH,
         fil,
         fi,
         fo_FO,
         fo,
         fr_BE,
         fr_CA,
         fr_CH,
         fr_LU,
         fr_MC,
         fr_SN,
         fur_IT,
         fur,
         gaa_GH,
         gaa,
         ga_IE,
         ga,
         gez_ER,
         gez_ET,
         gez,
         gl_ES,
         gl,
         gu_IN,
         gu,
         gv_GB,
         gv,
         ha_Arab_NG,
         ha_Arab_SD,
         ha_Arab,
         ha_GH,
         ha_Latn_GH,
         ha_Latn_NE,
         ha_Latn_NG,
         ha_Latn,
         ha_NE,
         ha_NG,
         ha_SD,
         haw_US,
         haw,
         ha,
         he_IL,
         he,
         hi_IN,
         hi,
         hr_HR,
         hr,
         hu_HU,
         hu,
         hy_AM_REVISED,
         hy_AM,
         hy,
         ia,
         id_ID,
         id,
         ig_NG,
         ig,
         ii_CN,
         ii,
         in,
         is_IS,
         is,
         it_CH,
         iu,
         iw,
         ja_JP,
         ja,
         ka_GE,
         kaj_NG,
         kaj,
         kam_KE,
         kam,
         ka,
         kcg_NG,
         kcg,
         kfo_CI,
         kfo,
         kk_Cyrl_KZ,
         kk_Cyrl,
         kk_KZ,
         kk,
         kl_GL,
         kl,
         km_KH,
         km,
         kn_IN,
         kn,
         kok_IN,
         ko_KR,
         kok,
         ko,
         kpe_GN,
         kpe_LR,
         kpe,
         ku_Arab,
         ku_Latn_TR,
         ku_Latn,
         ku_TR,
         ku,
         kw_GB,
         kw,
         ky_KG,
         ky,
         ln_CD,
         ln_CG,
         ln,
         lo_LA,
         lo,
         lt_LT,
         lt,
         lv_LV,
         lv,
         mk_MK,
         mk,
         ml_IN,
         ml,
         mn_CN,
         mn_Cyrl_MN,
         mn_Cyrl,
         mn_MN,
         mn_Mong_CN,
         mn_Mong,
         mn,
         mo,
         mr_IN,
         mr,
         ms_BN,
         ms_MY,
         ms,
         mt_MT,
         mt,
         my_MM,
         my,
         nb_NO,
         nb,
         ne_IN,
         ne_NP,
         ne,
         nl_BE,
         nl_NL,
         nl,
         nn_NO,
         nn,
         no,
         nr,
         nr_ZA,
         nso,
         nso_ZA,
         ny_MW,
         ny,
         om_ET,
         om_KE,
         om,
         or_IN,
         or,
         pa_Arab_PK,
         pa_Arab,
         pa_Guru_IN,
         pa_Guru,
         pa_IN,
         pa_PK,
         pa,
         pl_PL,
         pl,
         ps_AF,
         ps,
         pt_PT,
         pt,
         ro_MD,
         ro_RO,
         ro,
         ru_RU,
         ru_UA,
         ru,
         rw_RW,
         rw,
         sa_IN,
         sa,
         se_FI,
         se_NO,
         se,
         sh_BA,
         sh_CS,
         sh,
         sh_YU,
         sid_ET,
         sid,
         si_LK,
         si,
         sk_SK,
         sk,
         sl_SI,
         sl,
         so_DJ,
         so_ET,
         so_KE,
         so_SO,
         so,
         sq_AL,
         sq,
         sr_BA,
         sr_CS,
         sr_Cyrl_BA,
         sr_Cyrl_CS,
         sr_Cyrl_ME,
         sr_Cyrl_RS,
         sr_Cyrl,
         sr_Cyrl_YU,
         sr_Latn_BA,
         sr_Latn_CS,
         sr_Latn_ME,
         sr_Latn_RS,
         sr_Latn,
         sr_Latn_YU,
         sr_ME,
         sr_RS,
         sr,
         sr_YU,
         ss_SZ,
         ss,
         ss_ZA,
         st_LS,
         st,
         st_ZA,
         sv_FI,
         sv_SE,
         sv,
         sw_KE,
         sw_TZ,
         sw,
         syr_SY,
         syr,
         ta_IN,
         ta,
         te_IN,
         te,
         tg_Cyrl_TJ,
         tg_Cyrl,
         tg_TJ,
         tg,
         th_TH,
         th,
         ti_ER,
         ti_ET,
         tig_ER,
         tig,
         ti,
         tl,
         tn,
         tn_ZA,
         to_TO,
         to,
         tr_TR,
         tr,
         ts,
         ts_ZA,
         tt_RU,
         tt,
         ug_Arab_CN,
         ug_Arab,
         ug_CN,
         ug,
         uk_UA,
         uk,
         ur_IN,
         ur_PK,
         ur,
         uz_AF,
         uz_Arab_AF,
         uz_Arab,
         uz_Cyrl_UZ,
         uz_Cyrl,
         uz_Latn_UZ,
         uz_Latn,
         uz_UZ,
         uz,
         ve,
         ve_ZA,
         vi_VN,
         vi,
         wal_ET,
         wal,
         wo_Latn_SN,
         wo_Latn,
         wo_SN,
         wo,
         xh,
         xh_ZA,
         yo_NG,
         yo,
         zh_CN,
         zh_Hans_CN,
         zh_Hans_HK,
         zh_Hans_MO,
         zh_Hans_SG,
         zh_Hans,
         zh_Hant_HK,
         zh_Hant_MO,
         zh_Hant_TW,
         zh_Hant,
         zh_HK,
         zh_MO,
         zh_SG,
         zh_TW,
         zh,
         zu,
         zu_ZA
     }
 
     public LOCALE defaultLocale = LOCALE.en;
 
     private final Map<LOCALE, Map<String, String>> locales = new HashMap<LOCALE, Map<String, String>>();
 
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // SINGLETON
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     protected static AbstractI18n I18N_instance = null;
 
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // LOCALES
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     protected static Map<String, String> getLocales() {
         return I18N_instance._getLocales();
     }
 
     protected final Map<String, String> _getLocales() {
         final Map<String, String> locales = new LinkedHashMap<String, String>();
 
         locales.put("en_US", "U.S. English");
         locales.put("en", "English");
         locales.put("fr_FR", "français (France)");
         locales.put("fr", "Français");
         locales.put("es_ES", "español de España");
         locales.put("es", "Español");
         locales.put("it_IT", "italiano (Italia)");
         locales.put("it", "Italiano");
         locales.put("de_DE", "Deutsch (Deutschland)");
         locales.put("de", "Deutsch");
        // locales.put("pt_BR", "português do Brasil");
         locales.put("pt_BR", "Português (Brasil)");
         locales.put("aa_DJ", "Qafar (Yabuuti)");
         locales.put("aa_ER_SAAHO", "Qafar - Eretria (Saho)");
         locales.put("aa_ER", "Qafar (Eretria)");
         locales.put("aa_ET", "Qafar (Otobbia)");
         locales.put("aa", "Qafar");
         locales.put("af_NA", "Afrikaans (Namibië)");
         locales.put("af", "Afrikaans");
         locales.put("af_ZA", "Afrikaans (Suid-Afrika)");
         locales.put("ak_GH", "Akan (Ghana)");
         locales.put("ak", "Akan");
         locales.put("am_ET", "አማርኛ (ኢትዮጵያ)");
         locales.put("am", "አማርኛ");
         locales.put("ar_AE", "العربية (الامارات العربية المتحدة)");
         locales.put("ar_BH", "العربية (البحرين)");
         locales.put("ar_DZ", "العربية (الجزائر)");
         locales.put("ar_EG", "العربية (مصر)");
         locales.put("ar_IQ", "العربية (العراق)");
         locales.put("ar_JO", "العربية (الأردن)");
         locales.put("ar_KW", "العربية (الكويت)");
         locales.put("ar_LB", "العربية (لبنان)");
         locales.put("ar_LY", "العربية (ليبيا)");
         locales.put("ar_MA", "العربية (المغرب)");
         locales.put("ar_OM", "العربية (عمان)");
         locales.put("ar_QA", "العربية (قطر)");
         locales.put("ar_SA", "العربية (المملكة العربية السعودية)");
         locales.put("ar_SD", "العربية (السودان)");
         locales.put("ar_SY", "العربية (سوريا)");
         locales.put("ar_TN", "العربية (تونس)");
         locales.put("ar", "العربية");
         locales.put("ar_YE", "العربية (اليمن)");
         locales.put("as_IN", "অসমীয়া (ভাৰত)");
         locales.put("as", "অসমীয়া");
         locales.put("az_AZ", "azərbaycanca - latın (Azərbaycan)");
         locales.put("az_Cyrl_AZ", "Азәрбајҹан - kiril (Азәрбајҹан)");
         locales.put("az_Cyrl", "Азәрбајҹан (kiril)");
         locales.put("az_Latn_AZ", "azərbaycanca - latın (Azərbaycan)");
         locales.put("az", "azərbaycanca");
         locales.put("be_BY", "беларуская (Беларусь)");
         locales.put("be", "беларуская");
         locales.put("bg_BG", "български (България)");
         locales.put("bg", "български");
         locales.put("bn_BD", "বাংলা (বাংলাদেশ)");
         locales.put("bn_IN", "বাংলা (ভারত)");
         locales.put("bn", "বাংলা");
         locales.put("bs_BA", "bosanski (Bosna i Hercegovina)");
         locales.put("bs", "bosanski");
         locales.put("byn_ER", "ብሊን (ኤርትራ)");
         locales.put("byn", "ብሊን");
         locales.put("ca_ES", "català (Espanya)");
         locales.put("ca", "català");
         locales.put("cch_NG", "Atsam (Nigeria)");
         locales.put("cch", "Atsam");
         locales.put("cop", "Coptic");
         locales.put("cs_CZ", "čeština (Česká republika)");
         locales.put("cs", "čeština");
         locales.put("cy_GB", "Cymraeg (Prydain Fawr)");
         locales.put("cy", "Cymraeg");
         locales.put("da_DK", "dansk (Danmark)");
         locales.put("da", "dansk");
         locales.put("de_AT", "Österreichisches Deutsch");
         locales.put("de_BE", "Deutsch (Belgien)");
         locales.put("de_CH", "Schweizer Hochdeutsch");
         locales.put("de_LI", "Deutsch (Liechtenstein)");
         locales.put("de_LU", "Deutsch (Luxemburg)");
         locales.put("dv_MV", "ދިވެހިބަސް (ދިވެހި ރާއްޖެ)");
         locales.put("dv", "ދިވެހިބަސް");
         locales.put("dz_BT", "རྫོང་ཁ (འབྲུག)");
         locales.put("dz", "རྫོང་ཁ");
         locales.put("ee_GH", "Ewe (Ghana)");
         locales.put("ee_TG", "Ewe (Togo)");
         locales.put("ee", "Ewe");
         locales.put("el_CY", "Ελληνικά (Κύπρος)");
         locales.put("el_GR", "Ελληνικά (Ελλάδα)");
         locales.put("el_POLYTON", "Ἑλληνικά (Πολυτονικό)");
         locales.put("el", "Ελληνικά");
         locales.put("en_AS", "English (American Samoa)");
         locales.put("en_AU", "Australian English");
         locales.put("en_BE", "English (Belgium)");
         locales.put("en_BW", "English (Botswana)");
         locales.put("en_BZ", "English (Belize)");
         locales.put("en_CA", "Canadian English");
         locales.put("en_Dsrt_US", "𐐀𐑍𐑊𐐮𐑇 - 𐐔𐐯𐑆𐐲𐑉𐐯𐐻 (𐐏𐐭𐑌𐐴𐐻𐐲𐐼 𐐝𐐻𐐩𐐻𐑅)");
         locales.put("en_Dsrt", "𐐀𐑍𐑊𐐮𐑇 (𐐔𐐯𐑆𐐲𐑉𐐯𐐻)");
         locales.put("en_GB", "British English");
         locales.put("en_GU", "English (Guam)");
         locales.put("en_HK", "English (Hong Kong SAR China)");
         locales.put("en_IE", "English (Ireland)");
         locales.put("en_IN", "English (India)");
         locales.put("en_JM", "English (Jamaica)");
         locales.put("en_MH", "English (Marshall Islands)");
         locales.put("en_MP", "English (Northern Mariana Islands)");
         locales.put("en_MT", "English (Malta)");
         locales.put("en_NA", "English (Namibia)");
         locales.put("en_NZ", "English (New Zealand)");
         locales.put("en_PH", "English (Philippines)");
         locales.put("en_PK", "English (Pakistan)");
         locales.put("en_SG", "English (Singapore)");
         locales.put("en_Shaw", "English (Shavian)");
         locales.put("en_TT", "English (Trinidad and Tobago)");
         locales.put("en_UM", "English (United States Minor Outlying Islands)");
         locales.put("en_US_POSIX", "U.S. English (Computer)");
         locales.put("en_VI", "English (U.S. Virgin Islands)");
         locales.put("en_ZA", "English (South Africa)");
         locales.put("en_ZW", "English (Zimbabwe)");
         locales.put("eo", "esperanto");
         locales.put("es_AR", "español (Argentina)");
         locales.put("es_BO", "español (Bolivia)");
         locales.put("es_CL", "español (Chile)");
         locales.put("es_CO", "español (Colombia)");
         locales.put("es_CR", "español (Costa Rica)");
         locales.put("es_DO", "español (República Dominicana)");
         locales.put("es_EC", "español (Ecuador)");
         locales.put("es_GT", "español (Guatemala)");
         locales.put("es_HN", "español (Honduras)");
         locales.put("es_MX", "español (México)");
         locales.put("es_NI", "español (Nicaragua)");
         locales.put("es_PA", "español (Panamá)");
         locales.put("es_PE", "español (Perú)");
         locales.put("es_PR", "español (Puerto Rico)");
         locales.put("es_PY", "español (Paraguay)");
         locales.put("es_SV", "español (El Salvador)");
         locales.put("es_US", "español (Estados Unidos)");
         locales.put("es_UY", "español (Uruguay)");
         locales.put("es_VE", "español (Venezuela)");
         locales.put("et_EE", "eesti (Eesti)");
         locales.put("et", "eesti");
         locales.put("eu_ES", "euskara (Espainia)");
         locales.put("eu", "euskara");
         locales.put("fa_AF", "دری (افغانستان)");
         locales.put("fa_IR", "فارسی (ایران)");
         locales.put("fa", "فارسی");
         locales.put("fi_FI", "suomi (Suomi)");
         locales.put("fil_PH", "Filipino (Pilipinas)");
         locales.put("fil", "Filipino");
         locales.put("fi", "suomi");
         locales.put("fo_FO", "føroyskt (Føroyar)");
         locales.put("fo", "føroyskt");
         locales.put("fr_BE", "français (Belgique)");
         locales.put("fr_CA", "français canadien");
         locales.put("fr_CH", "français suisse");
         locales.put("fr_LU", "français (Luxembourg)");
         locales.put("fr_MC", "français (Monaco)");
         locales.put("fr_SN", "français (Sénégal)");
         locales.put("fur_IT", "furlan (Italie)");
         locales.put("fur", "furlan");
         locales.put("gaa_GH", "Ga (Ghana)");
         locales.put("gaa", "Ga");
         locales.put("ga_IE", "Gaeilge (Éire)");
         locales.put("ga", "Gaeilge");
         locales.put("gez_ER", "ግዕዝኛ (ኤርትራ)");
         locales.put("gez_ET", "ግዕዝኛ (ኢትዮጵያ)");
         locales.put("gez", "ግዕዝኛ");
         locales.put("gl_ES", "galego (España)");
         locales.put("gl", "galego");
         locales.put("gu_IN", "ગુજરાતી (ભારત)");
         locales.put("gu", "ગુજરાતી");
         locales.put("gv_GB", "Gaelg (Rywvaneth Unys)");
         locales.put("gv", "Gaelg");
         locales.put("ha_Arab_NG", "Haoussa - Arabic (Nijeriya)");
         locales.put("ha_Arab_SD", "Haoussa - Arabic (Sudan)");
         locales.put("ha_Arab", "Haoussa (Arabic)");
         locales.put("ha_GH", "Haoussa - Latin (Ghana)");
         locales.put("ha_Latn_GH", "Haoussa - Latin (Ghana)");
         locales.put("ha_Latn_NE", "Haoussa - Latin (Niger)");
         locales.put("ha_Latn_NG", "Haoussa - Latin (Nijeriya)");
         locales.put("ha_Latn", "Haoussa (Latin)");
         locales.put("ha_NE", "Haoussa - Latin (Niger)");
         locales.put("ha_NG", "Haoussa - Latin (Nijeriya)");
         locales.put("ha_SD", "Haoussa - Arabic (Sudan)");
         locales.put("haw_US", "ʻōlelo Hawaiʻi (ʻAmelika Hui Pū ʻIa)");
         locales.put("haw", "ʻōlelo Hawaiʻi");
         locales.put("ha", "Haoussa");
         locales.put("he_IL", "עברית (ישראל)");
         locales.put("he", "עברית");
         locales.put("hi_IN", "हिन्दी (भारत)");
         locales.put("hi", "हिन्दी");
         locales.put("hr_HR", "hrvatski (Hrvatska)");
         locales.put("hr", "hrvatski");
         locales.put("hu_HU", "magyar (Magyarország)");
         locales.put("hu", "magyar");
         locales.put("hy_AM_REVISED", "Հայերէն - Հայաստանի Հանրապետութիւն (Revised Orthography)");
         locales.put("hy_AM", "Հայերէն (Հայաստանի Հանրապետութիւն)");
         locales.put("hy", "Հայերէն");
         locales.put("ia", "interlingua");
         locales.put("id_ID", "Bahasa Indonesia (Indonesia)");
         locales.put("id", "Bahasa Indonesia");
         locales.put("ig_NG", "Igbo (Nigeria)");
         locales.put("ig", "Igbo");
         locales.put("ii_CN", "ꆈꌠꉙ (ꍏꇩ)");
         locales.put("ii", "ꆈꌠꉙ");
         locales.put("in", "Bahasa Indonesia");
         locales.put("is_IS", "íslenska (Ísland)");
         locales.put("is", "íslenska");
         locales.put("it_CH", "italiano (Svizzera)");
         locales.put("iu", "ᐃᓄᒃᑎᑐᑦ ᑎᑎᕋᐅᓯᖅ");
         locales.put("iw", "עברית");
         locales.put("ja_JP", "日本語 (日本)");
         locales.put("ja", "日本語");
         locales.put("ka_GE", "ქართული (საქართველო)");
         locales.put("kaj_NG", "Jju (Nigeria)");
         locales.put("kaj", "Jju");
         locales.put("kam_KE", "Kamba (Kenya)");
         locales.put("kam", "Kamba");
         locales.put("ka", "ქართული");
         locales.put("kcg_NG", "Tyap (Nigeria)");
         locales.put("kcg", "Tyap");
         locales.put("kfo_CI", "Koro (Ivory Coast)");
         locales.put("kfo", "Koro");
         locales.put("kk_Cyrl_KZ", "Қазақ - Cyrillic (Қазақстан)");
         locales.put("kk_Cyrl", "Қазақ (Cyrillic)");
         locales.put("kk_KZ", "Қазақ - Cyrillic (Қазақстан)");
         locales.put("kk", "Қазақ");
         locales.put("kl_GL", "kalaallisut (Kalaallit Nunaat)");
         locales.put("kl", "kalaallisut");
         locales.put("km_KH", "ភាសាខ្មែរ (កម្ពុជា)");
         locales.put("km", "ភាសាខ្មែរ");
         locales.put("kn_IN", "ಕನ್ನಡ (ಭಾರತ)");
         locales.put("kn", "ಕನ್ನಡ");
         locales.put("kok_IN", "कोंकणी (भारत)");
         locales.put("ko_KR", "한국어 (대한민국)");
         locales.put("kok", "कोंकणी");
         locales.put("ko", "한국어");
         locales.put("kpe_GN", "Kpelle (Guinea)");
         locales.put("kpe_LR", "Kpelle (Liberia)");
         locales.put("kpe", "Kpelle");
         locales.put("ku_Arab", "كوردی (Arabic)");
         locales.put("ku_Latn_TR", "kurdî - Latin (Tirkiye)");
         locales.put("ku_Latn", "kurdî (Latin)");
         locales.put("ku_TR", "كوردی - Latin (Turkey)");
         locales.put("ku", "كوردی");
         locales.put("kw_GB", "kernewek (Rywvaneth Unys)");
         locales.put("kw", "kernewek");
         locales.put("ky_KG", "Кыргыз (Кыргызстан)");
         locales.put("ky", "Кыргыз");
         locales.put("ln_CD", "lingála (Kongó-Kinsásá)");
         locales.put("ln_CG", "lingála (Kongó-Brazzaville)");
         locales.put("ln", "lingála");
         locales.put("lo_LA", "ລາວ (ລາວ)");
         locales.put("lo", "ລາວ");
         locales.put("lt_LT", "lietuvių (Lietuva)");
         locales.put("lt", "lietuvių");
         locales.put("lv_LV", "latviešu (Latvija)");
         locales.put("lv", "latviešu");
         locales.put("mk_MK", "македонски (Македонија)");
         locales.put("mk", "македонски");
         locales.put("ml_IN", "മലയാളം (ഇന്ത്യ)");
         locales.put("ml", "മലയാളം");
         locales.put("mn_CN", "монгол - Mongolian (China)");
         locales.put("mn_Cyrl_MN", "монгол - Cyrillic (Монгол улс)");
         locales.put("mn_Cyrl", "монгол (Cyrillic)");
         locales.put("mn_MN", "монгол - Cyrillic (Монгол улс)");
         locales.put("mn_Mong_CN", "монгол - Mongolian (China)");
         locales.put("mn_Mong", "монгол (Mongolian)");
         locales.put("mn", "монгол");
         locales.put("mo", "Moldavian");
         locales.put("mr_IN", "मराठी (भारत)");
         locales.put("mr", "मराठी");
         locales.put("ms_BN", "Bahasa Melayu (Brunei)");
         locales.put("ms_MY", "Bahasa Melayu (Malaysia)");
         locales.put("ms", "Bahasa Melayu");
         locales.put("mt_MT", "Malti (Malta)");
         locales.put("mt", "Malti");
         locales.put("my_MM", "ဗမာ (မြန်မာ)");
         locales.put("my", "ဗမာ");
         locales.put("nb_NO", "norsk bokmål (Norge)");
         locales.put("nb", "norsk bokmål");
         locales.put("ne_IN", "नेपाली (भारत)");
         locales.put("ne_NP", "नेपाली (नेपाल)");
         locales.put("ne", "नेपाली");
         locales.put("nl_BE", "Vlaams");
         locales.put("nl_NL", "Nederlands (Nederland)");
         locales.put("nl", "Nederlands");
         locales.put("nn_NO", "nynorsk (Noreg)");
         locales.put("nn", "nynorsk");
         locales.put("no", "norsk bokmål");
         locales.put("nr", "isiNdebele");
         locales.put("nr_ZA", "isiNdebele (South Africa)");
         locales.put("nso", "Sesotho sa Leboa");
         locales.put("nso_ZA", "Sesotho sa Leboa (South Africa)");
         locales.put("ny_MW", "Nyanja (Malawi)");
         locales.put("ny", "Nyanja");
         locales.put("om_ET", "Oromoo (Itoophiyaa)");
         locales.put("om_KE", "Oromoo (Keeniyaa)");
         locales.put("om", "Oromoo");
         locales.put("or_IN", "ଓଡ଼ିଆ (ଭାରତ)");
         locales.put("or", "ଓଡ଼ିଆ");
         locales.put("pa_Arab_PK", "پنجاب - العربية (پکستان)");
         locales.put("pa_Arab", "پنجاب (العربية)");
         locales.put("pa_Guru_IN", "ਪੰਜਾਬੀ - ਗੁਰਮੁਖੀ (ਭਾਰਤ)");
         locales.put("pa_Guru", "ਪੰਜਾਬੀ (ਗੁਰਮੁਖੀ)");
         locales.put("pa_IN", "ਪੰਜਾਬੀ - ਗੁਰਮੁਖੀ (ਭਾਰਤ)");
         locales.put("pa_PK", "ਪੰਜਾਬੀ - Arabic (Pakistan)");
         locales.put("pa", "ਪੰਜਾਬੀ");
         locales.put("pl_PL", "polski (Polska)");
         locales.put("pl", "polski");
         locales.put("ps_AF", "پښتو (افغانستان)");
         locales.put("ps", "پښتو");
         locales.put("pt_PT", "português europeu");
         locales.put("pt", "português");
         locales.put("ro_MD", "română (Moldova, Republica)");
         locales.put("ro_RO", "română (România)");
         locales.put("ro", "română");
         locales.put("ru_RU", "русский (Россия)");
         locales.put("ru_UA", "русский (Украина)");
         locales.put("ru", "русский");
         locales.put("rw_RW", "Kinyarwanda (Rwanda)");
         locales.put("rw", "Kinyarwanda");
         locales.put("sa_IN", "संस्कृत भाषा (भारतम्)");
         locales.put("sa", "संस्कृत भाषा");
         locales.put("se_FI", "se (FI)");
         locales.put("se_NO", "davvisámegiella (Norga)");
         locales.put("se", "davvisámegiella");
         locales.put("sh_BA", "Srpski - Latinica (Bosna i Hercegovina)");
         locales.put("sh_CS", "Srpski - Latinica (Srbija)");
         locales.put("sh", "Srpski (Latinica)");
         locales.put("sh_YU", "Srpski - Latinica (Srbija)");
         locales.put("sid_ET", "Sidaamu Afo (Itiyoophiya)");
         locales.put("sid", "Sidaamu Afo");
         locales.put("si_LK", "සිංහල (ශ්‍රී ලංකාව)");
         locales.put("si", "සිංහල");
         locales.put("sk_SK", "slovenský (Slovenská republika)");
         locales.put("sk", "slovenský");
         locales.put("sl_SI", "slovenščina (Slovenija)");
         locales.put("sl", "slovenščina");
         locales.put("so_DJ", "Soomaali (Jabuuti)");
         locales.put("so_ET", "Soomaali (Itoobiya)");
         locales.put("so_KE", "Soomaali (Kiiniya)");
         locales.put("so_SO", "Soomaali (Soomaaliya)");
         locales.put("so", "Soomaali");
         locales.put("sq_AL", "shqipe (Shqipëria)");
         locales.put("sq", "shqipe");
         locales.put("sr_BA", "српски - Ћирилица (Босна и Херцеговина)");
         locales.put("sr_CS", "Српски - Ћирилица (Србија)");
         locales.put("sr_Cyrl_BA", "српски - Ћирилица (Босна и Херцеговина)");
         locales.put("sr_Cyrl_CS", "Српски - Ћирилица (Србија)");
         locales.put("sr_Cyrl_ME", "Српски - Ћирилица (Црна Гора)");
         locales.put("sr_Cyrl_RS", "Српски - Ћирилица (Србија)");
         locales.put("sr_Cyrl", "Српски (Ћирилица)");
         locales.put("sr_Cyrl_YU", "Српски - Ћирилица (Србија)");
         locales.put("sr_Latn_BA", "Srpski - Latinica (Bosna i Hercegovina)");
         locales.put("sr_Latn_CS", "Srpski - Latinica (Srbija)");
         locales.put("sr_Latn_ME", "Srpski - Latinica (Crna Gora)");
         locales.put("sr_Latn_RS", "Srpski - Latinica (Srbija)");
         locales.put("sr_Latn", "Srpski (Latinica)");
         locales.put("sr_Latn_YU", "Srpski - Latinica (Srbija)");
         locales.put("sr_ME", "Српски - Ћирилица (Црна Гора)");
         locales.put("sr_RS", "Српски - Ћирилица (Србија)");
         locales.put("sr", "Српски");
         locales.put("sr_YU", "Српски - Ћирилица (Србија)");
         locales.put("ss_SZ", "Siswati (Swaziland)");
         locales.put("ss", "Siswati");
         locales.put("ss_ZA", "Siswati (South Africa)");
         locales.put("st_LS", "Sesotho (Lesotho)");
         locales.put("st", "Sesotho");
         locales.put("st_ZA", "Sesotho (South Africa)");
         locales.put("sv_FI", "svenska (Finland)");
         locales.put("sv_SE", "svenska (Sverige)");
         locales.put("sv", "svenska");
         locales.put("sw_KE", "Kiswahili (Kenya)");
         locales.put("sw_TZ", "Kiswahili (Tanzania)");
         locales.put("sw", "Kiswahili");
         locales.put("syr_SY", "ܣܘܪܝܝܐ (ܣܘܪܝܝܐ)");
         locales.put("syr", "ܣܘܪܝܝܐ");
         locales.put("ta_IN", "தமிழ் (இந்தியா)");
         locales.put("ta", "தமிழ்");
         locales.put("te_IN", "తెలుగు (భారత దేళం)");
         locales.put("te", "తెలుగు");
         locales.put("tg_Cyrl_TJ", "Tajik - Cyrillic (Tajikistan)");
         locales.put("tg_Cyrl", "Tajik (Cyrillic)");
         locales.put("tg_TJ", "Tajik (Tajikistan)");
         locales.put("tg", "Tajik");
         locales.put("th_TH", "ไทย (ไทย)");
         locales.put("th", "ไทย");
         locales.put("ti_ER", "ትግርኛ (Eritrea)");
         locales.put("ti_ET", "ትግርኛ (Ethiopia)");
         locales.put("tig_ER", "ትግረ (ኤርትራ)");
         locales.put("tig", "ትግረ");
         locales.put("ti", "ትግርኛ");
         locales.put("tl", "Filipino");
         locales.put("tn", "Setswana");
         locales.put("tn_ZA", "Setswana (South Africa)");
         locales.put("to_TO", "lea fakatonga (Tonga)");
         locales.put("to", "lea fakatonga");
         locales.put("tr_TR", "Türkçe (Türkiye)");
         locales.put("tr", "Türkçe");
         locales.put("ts", "Xitsonga");
         locales.put("ts_ZA", "Xitsonga (South Africa)");
         locales.put("tt_RU", "Татар (Россия)");
         locales.put("tt", "Татар");
         locales.put("ug_Arab_CN", "Uighur - Arabic (China)");
         locales.put("ug_Arab", "Uighur (Arabic)");
         locales.put("ug_CN", "Uighur (China)");
         locales.put("ug", "Uighur");
         locales.put("uk_UA", "українська (Україна)");
         locales.put("uk", "українська");
         locales.put("ur_IN", "اردو (بھارت)");
         locales.put("ur_PK", "اردو (پاکستان)");
         locales.put("ur", "اردو");
         locales.put("uz_AF", "Ўзбек - Араб (Афғонистон)");
         locales.put("uz_Arab_AF", "اۉزبېک - Араб (افغانستان)");
         locales.put("uz_Arab", "اۉزبېک (Араб)");
         locales.put("uz_Cyrl_UZ", "Ўзбек - Кирил (Ўзбекистон)");
         locales.put("uz_Cyrl", "Ўзбек (Кирил)");
         locales.put("uz_Latn_UZ", "o'zbekcha - Lotin (Oʿzbekiston)");
         locales.put("uz_Latn", "o'zbekcha (Lotin)");
         locales.put("uz_UZ", "Ўзбек - Кирил (Ўзбекистон)");
         locales.put("uz", "Ўзбек");
         locales.put("ve", "Tshivenḓa");
         locales.put("ve_ZA", "Tshivenḓa (South Africa)");
         locales.put("vi_VN", "Tiếng Việt (Việt Nam)");
         locales.put("vi", "Tiếng Việt");
         locales.put("wal_ET", "ወላይታቱ (ኢትዮጵያ)");
         locales.put("wal", "ወላይታቱ");
         locales.put("wo_Latn_SN", "Wolof - Latin (Senegal)");
         locales.put("wo_Latn", "Wolof (Latin)");
         locales.put("wo_SN", "Wolof (Senegal)");
         locales.put("wo", "Wolof");
         locales.put("xh", "isiXhosa");
         locales.put("xh_ZA", "isiXhosa (South Africa)");
         locales.put("yo_NG", "Yorùbá (NG)");
         locales.put("yo", "Yorùbá");
         locales.put("zh_CN", "中文（简体） (中国)");
         locales.put("zh_Hans_CN", "中文（简体） (中国)");
         locales.put("zh_Hans_HK", "中文（简体） (中国香港特别行政区)");
         locales.put("zh_Hans_MO", "中文（简体） (中国澳门特别行政区)");
         locales.put("zh_Hans_SG", "中文（简体） (新加坡)");
         locales.put("zh_Hans", "中文（简体）");
         locales.put("zh_Hant_HK", "繁體中文 (中華人民共和國香港特別行政區)");
         locales.put("zh_Hant_MO", "繁體中文 (中華人民共和國澳門特別行政區)");
         locales.put("zh_Hant_TW", "繁體中文 (臺灣)");
         locales.put("zh_Hant", "繁體中文");
         locales.put("zh_HK", "中文（繁体） (中国香港特别行政区)");
         locales.put("zh_MO", "中文（繁体） (中国澳门特别行政区)");
         locales.put("zh_SG", "中文（简体） (新加坡)");
         locales.put("zh_TW", "中文（繁体） (台湾)");
         locales.put("zh", "中文");
         locales.put("zu", "isiZulu");
         locales.put("zu_ZA", "isiZulu (South Africa)");
 
         return locales;
     }
 
     public Map<String, String> getLocale(final LOCALE locale) {
         if (locale != null) {
             if (!this.locales.containsKey(locale)) {
                 loadLocale(locale);
             }
             return this.locales.get(locale);
         } else {
             return getLocale(getDefaultLocale());
         }
     }
 
     protected final void setLocale(final LOCALE locale, final Map<String, String> map) {
         this.locales.put(locale, map);
     }
 
     public final static void load(final LOCALE locale) {
         I18N_instance.loadLocale(locale);
     }
 
     public abstract void loadLocale(LOCALE locale);
 
     public static boolean isLoaded(final LOCALE locale) {
         return I18N_instance.isLocaleLoaded(locale);
     }
 
     public final boolean isLocaleLoaded(final LOCALE locale) {
         return this.locales.containsKey(locale);
     }
 
     public static int countLoaded() {
         return I18N_instance.countLoadedLocales();
     }
 
     public final int countLoadedLocales() {
         return this.locales.size();
     }
 
     public static LOCALE getDefaultLocale() {
         return I18N_instance._getDefaultLocale();
     }
 
     public final LOCALE _getDefaultLocale() {
         return this.defaultLocale;
     }
 
     private void _setDefaultLocale(final LOCALE locale) {
         this.defaultLocale = locale;
     }
 
     public final static void setDefaultLocale(final LOCALE locale) {
         I18N_instance._setDefaultLocale(locale);
     }
 
     public final static LOCALE stringToLocale(final String localeString) {
 
         for (final LOCALE locale : LOCALE.values()) {
             if (locale.toString().equals(localeString)) {
                 return locale;
             }
         }
 
         return null;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // GETTEXT
     // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     protected boolean _isSet(final String string) {
         return isSet(string, this.defaultLocale);
     }
 
     protected boolean _isSet(final String string, final LOCALE locale) {
         final Map<String, String> localeMap = getLocale(locale);
 
         if (localeMap == null) {
             return false;
         }
 
         final String translation = localeMap.get(string);
         if (translation == null) {
             return false;
         }
 
         return true;
     }
 
     public static boolean isSet(final String string) {
         return I18N_instance._isSet(string);
     }
 
     public static boolean isSet(final String string, final LOCALE locale) {
         return I18N_instance._isSet(string, locale);
     }
 
     protected String getText(final String string) {
         return this.getText(string, this.defaultLocale);
     }
 
     protected String getText(final String string, final LOCALE locale) {
         final Map<String, String> localeMap = getLocale(locale);
 
         if (localeMap == null) {
             return string;
         }
 
         final String translation = localeMap.get(string);
         if (translation == null) {
             return string;
         }
 
         return translation;
     }
 
     protected String getText(final String string, final Arg... args) {
         return this.getText(string, this.defaultLocale, args);
     }
 
     protected String getText(final String string, final LOCALE locale, final Arg... args) {
         return new TextTemplate(_(string)).toString(args);
     }
 
     protected String getText(final String string, final IItem item) {
         return this.getText(string, this.defaultLocale, item);
     }
 
     protected String getText(final String string, final LOCALE locale, final IItem item) {
         return new TextTemplate(string).toString(item.getAttributes());
     }
 
     public static String _(final String string) {
        return string.isEmpty() ? "" : I18N_instance.getText(string);
     }
 
     public static String _(final String string, final LOCALE locale) {
        return string.isEmpty() ? "" : I18N_instance.getText(string, locale);
     }
 
     public static String _(final String string, final Arg... args) {
        return string.isEmpty() ? "" : I18N_instance.getText(string, args);
     }
 
     public static String _(final String string, final LOCALE locale, final Arg... args) {
        return string.isEmpty() ? "" : I18N_instance.getText(string, locale, args);
     }
 
     public static String _(final String string, final IItem item) {
        return string.isEmpty() ? "" : I18N_instance.getText(string, item);
     }
 
     public static String _(final String string, final LOCALE locale, final IItem item) {
        return string.isEmpty() ? "" : I18N_instance.getText(string, locale, item);
     }
 
 }
