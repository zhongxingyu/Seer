 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.structure;
 
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.SelectableObjectWithName;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.value.*;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * [fleXive] data types
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public enum FxDataType implements Serializable, SelectableObjectWithName {
     HTML(1, true, true, true, FxHTML.class),
     String1024(2, true, true, true, FxString.class),
     Text(3, true, true, true, FxString.class),
     Number(4, true, true, false, FxNumber.class),
     LargeNumber(5, true, true, false, FxLargeNumber.class),
     Float(6, true, true, false, FxFloat.class),
     Double(7, true, true, false, FxDouble.class),
     Date(8, false, true, false, FxDate.class),
     DateTime(9, false, true, false, FxDateTime.class),
     DateRange(10, false, true, false, FxDateRange.class),
     DateTimeRange(11, false, true, false, FxDateTimeRange.class),
     Boolean(12, true, true, false, FxBoolean.class),
     Binary(13, false, true, true, FxBinary.class),
     Reference(14, false, true, false, FxReference.class),
     InlineReference(15, false, false, false, FxReference.class),
     SelectOne(16, false, true, true, FxSelectOne.class),
     SelectMany(17, false, true, true, FxSelectMany.class);
 
     private boolean initialized;
     private final long id;
     private String name;
     private FxString label;
     private final boolean singleRowStorage;
     private final boolean primitiveValueType;
     private final Class<? extends FxValue> valueClass;
 
     private transient final static String RANDOM_TEXT = "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? [33] At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. et harum quidem rerum facilis est et expedita distinctio. nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.   [34] Hanc ego cum teneam sententiam, quid est cur verear, ne ad eam non possim accommodare Torquatos nostros? quos tu paulo ante cum memoriter, tum etiam erga nos amice et benivole collegisti, nec me tamen laudandis maioribus meis corrupisti nec segniorem ad respondendum reddidisti. quorum facta quem ad modum, quaeso, interpretaris? sicine eos censes aut in armatum hostem impetum fecisse aut in liberos atque in sanguinem suum tam crudelis fuisse, nihil ut de utilitatibus, nihil ut de commodis suis cogitarent? at id ne ferae quidem faciunt, ut ita ruant itaque turbent, ut earum motus et impetus quo pertineant non intellegamus, tu tam egregios viros censes tantas res gessisse sine causa? [35] quae fuerit causa, mox videro; interea hoc tenebo, si ob aliquam causam ista, quae sine dubio praeclara sunt, fecerint, virtutem iis per se ipsam causam non fuisse. -- Torquem detraxit hosti. -- Et quidem se texit, ne interiret. -- At magnum periculum adiit. -- In oculis quidem exercitus. -- Quid ex eo est consecutus? -- Laudem et caritatem, quae sunt vitae sine metu degendae praesidia firmissima. -- Filium morte multavit. -- Si sine causa, nollem me ab eo ortum, tam inportuno tamque crudeli; sin, ut dolore suo sanciret militaris imperii disciplinam exercitumque in gravissimo bello animadversionis metu contineret, saluti prospexit civium, qua intellegebat contineri suam. atque haec ratio late patet. [36] in quo enim maxime consuevit iactare vestra se oratio, tua praesertim, qui studiose antiqua persequeris, claris et fortibus viris commemorandis eorumque factis non emolumento aliquo, sed ipsius honestatis decore laudandis, id totum evertitur eo delectu rerum, quem modo dixi, constituto, ut aut voluptates omittantur maiorum voluptatum adipiscendarum causa aut dolores suscipiantur maiorum dolorum effugiendorum gratia.   [37] Sed de clarorum hominum factis illustribus et gloriosis satis hoc loco dictum sit. erit enim iam de omnium virtutum cursu ad voluptatem proprius disserendi locus. nunc autem explicabo, voluptas ipsa quae qualisque sit, ut tollatur error omnis imperitorum intellegaturque ea, quae voluptaria, delicata, mollis habeatur disciplina, quam gravis, quam continens, quam severa sit. Non enim hanc solam sequimur, quae suavitate aliqua naturam ipsam movet et cum iucunditate quadam percipitur sensibus, sed maximam voluptatem illam habemus, quae percipitur omni dolore detracto, nam quoniam, cum privamur dolore, ipsa liberatione et vacuitate omnis molestiae gaudemus, omne autem id, quo gaudemus, voluptas est, ut omne, quo offendimur, dolor, doloris omnis privatio recte nominata est voluptas. ut enim, cum cibo et potione fames sitisque depulsa est, ipsa detractio molestiae consecutionem affert voluptatis, sic in omni re doloris amotio successionem efficit voluptatis. [38] itaque non placuit Epicuro medium esse se quiddam inter dolorem et voluptatem; illud enim ipsum, quod quibusdam medium videretur, cum omni dolore careret, non modo voluptatem esse, verum etiam summam voluptatem. quisquis enim sentit, quem ad modum sit affectus, eum necesse est aut in voluptate esse aut in dolore. omnis autem privatione doloris putat Epicurus terminari summam voluptatem, ut postea variari voluptas distinguique possit, augeri amplificarique non possit. [ 39] At etiam Athenis, ut e patre audiebam facete et urbane Stoicos irridente, statua est in Ceramico Chrysippi sedentis porrecta manu, quae manus significet illum in hae esse rogatiuncula delectatum: 'Numquidnam manus tua sic affecta, quem ad modum affecta nunc est, desiderat?' -- Nihil sane. -- 'At, si voluptas esset bonum, desideraret.' -- Ita credo. -- 'Non est igitur voluptas bonum.' Hoc ne statuam quidem dicturam pater aiebat, si loqui posset. conclusum est enim contra Cyrenaicos satis acute, nihil ad Epicurum. nam si ea sola voluptas esset, quae quasi titillaret sensus, ut ita dicam, et ad eos cum suavitate afflueret et illaberetur, nec manus esse contenta posset nec ulla pars vacuitate doloris sine iucundo motu voluptatis. sin autem summa voluptas est, ut Epicuro placet, nihil dolere, primum tibi recte, Chrysippe, concessum est nihil desiderare manum, cum ita esset affecta, secundum non recte, si voluptas esset bonum, fuisse desideraturam. idcirco enim non desideraret, quia, quod dolore caret, id in voluptate est.   [40] Extremum autem esse bonorum voluptatem ex hoc facillime perspici potest: Constituamus aliquem magnis, multis, perpetuis fruentem et animo et corpore voluptatibus nullo dolore nec impediente nec inpendente, quem tandem hoc statu praestabiliorem aut magis expetendum possimus dicere? inesse enim necesse est in eo, qui ita sit affectus, et firmitatem animi nec mortem nec dolorem timentis, quod mors sensu careat, dolor in longinquitate levis, in gravitate brevis soleat esse, ut eius magnitudinem celeritas, diuturnitatem allevatio consoletur. [41] ad ea cum accedit, ut neque divinum numen horreat nec praeteritas voluptates effluere patiatur earumque assidua recordatione laetetur, quid est, quod huc possit, quod melius sit, accedere? Statue contra aliquem confectum tantis animi corporisque doloribus, quanti in hominem maximi cadere possunt, nulla spe proposita fore levius aliquando, nulla praeterea neque praesenti nec expectata voluptate, quid eo miserius dici aut fingi potest? quodsi vita doloribus referta maxime fugienda est, summum profecto malum est vivere cum dolore, cui sententiae consentaneum est ultimum esse bonorum eum voluptate vivere. nec enim habet nostra mens quicquam, ubi consistat tamquam in extremo, omnesque et metus et aegritudines ad dolorem referuntur, nec praeterea est res ulla, quae sua natura aut sollicitare possit aut angere. [42] Praeterea et appetendi et refugiendi et omnino rerum gerendarum initia proficiscuntur aut a voluptate aut a dolore. quod cum ita sit, perspicuum est omnis rectas res atque laudabilis eo referri, ut cum voluptate vivatur. quoniam autem id est vel summum bonorum vel ultimum vel extremum -- quod Graeci telos nominant --, quod ipsum nullam ad aliam rem, ad id autem res referuntur omnes, fatendum est summum esse bonum iucunde vivere.   Id qui in una virtute ponunt et splendore nominis capti quid natura postulet non intellegunt, errore maximo, si Epicurum audire voluerint, liberabuntur: istae enim vestrae eximiae pulchraeque virtutes nisi voluptatem efficerent, quis eas aut laudabilis aut expetendas arbitraretur? ut enim medicorum scientiam non ipsius artis, sed bonae valetudinis causa probamus, et gubernatoris ars, quia bene navigandi rationem habet, utilitate, non arte laudatur, sic sapientia, quae ars vivendi putanda est, non expeteretur, si nihil efficeret; nunc expetitur, quod est tamquam artifex conquirendae et comparandae voluptatis -- [43] quam autem ego dicam voluptatem, iam videtis, ne invidia verbi labefactetur oratio mea --. nam cum ignoratione rerum bonarum et malarum maxime hominum vita vexetur, ob eumque errorem et voluptatibus maximis saepe priventur et durissimis animi doloribus torqueantur, sapientia est adhibenda, quae et terroribus cupiditatibusque detractis et omnium falsarum opinionum temeritate derepta certissimam se nobis ducem praebeat ad voluptatem. sapientia enim est una, quae maestitiam pellat ex animis, quae nos exhorrescere metu non sinat. qua praeceptrice in tranquillitate vivi potest omnium cupiditatum ardore restincto. cupiditates enim sunt insatiabiles, quae non modo singulos homines, sed universas familias evertunt, totam etiam labefactant saepe rem publicam. [44] ex cupiditatibus odia, discidia, discordiae, seditiones, bella nascuntur, nec eae se foris solum iactant nec tantum in alios caeco impetu incurrunt, sed intus etiam in animis inclusae inter se dissident atque discordant, ex quo vitam amarissimam necesse est effici, ut sapiens solum amputata circumcisaque inanitate omni et errore naturae finibus contentus sine aegritudine possit et sine metu vivere. [45] quae est enim aut utilior aut ad bene vivendum aptior partitio quam illa, qua est usus Epicurus? qui unum genus posuit earum cupiditatum, quae essent et naturales et necessariae, alterum, quae naturales essent nec tamen necessariae, tertium, quae nec naturales nec necessariae. quarum ea ratio est, ut necessariae nec opera multa nec impensa expleantur; ne naturales quidem multa desiderant, propterea quod ipsa natura divitias, quibus contenta sit, et parabilis et terminatas habet; inanium autem cupiditatum nec modus ullus nec finis inveniri potest. [46] quodsi vitam omnem perturbari videmus errore et inscientia, sapientiamque esse solam, quae nos a libidinum impetu et a formidinum terrore vindicet et ipsius fortunae modice ferre doceat iniurias et omnis monstret vias, quae ad quietem et ad tranquillitatem ferant, quid est cur dubitemus dicere et sapientiam propter voluptates expetendam et insipientiam propter molestias esse fugiendam?   [47] Eademque ratione ne temperantiam quidem propter se expetendam esse dicemus, sed quia pacem animis afferat et eos quasi concordia quadam placet ac leniat. temperantia est enim, quae in rebus aut expetendis aut fugiendis ut rationem sequamur monet. nec enim satis est iudicare quid faciendum non faciendumve sit, sed stare etiam oportet in eo, quod sit iudicatum. plerique autem, quod tenere atque servare id, quod ipsi statuerunt, non possunt, victi et debilitati obiecta specie voluptatis tradunt se libidinibus constringendos nec quid eventurum sit provident ob eamque causam propter voluptatem et parvam et non necessariam et quae vel aliter pararetur et qua etiam carere possent sine dolore tum in morbos gravis, tum in damna, tum in dedecora incurrunt, saepe etiam legum iudiciorumque poenis obligantur. [48] Qui autem ita frui volunt voluptatibus, ut nulli propter eas consequantur dolores, et qui suum iudicium retinent, ne voluptate victi faciant id, quod sentiant non esse faciendum, ii voluptatem maximam adipiscuntur praetermittenda voluptate. idem etiam dolorem saepe perpetiuntur, ne, si id non faciant, incidant in maiorem. ex quo intellegitur nec intemperantiam propter se esse fugiendam temperantiamque expetendam, non quia voluptates fugiat, sed quia maiores consequatur.   [49] Eadem fortitudinis ratio reperietur. nam neque laborum perfunctio neque perpessio dolorum per se ipsa allicit nec patientia nec assiduitas nec vigiliae nec ea ipsa, quae laudatur, industria, ne fortitudo quidem, sed ista sequimur, ut sine cura metuque vivamus animumque et corpus, quantum efficere possimus, molestia liberemus. ut enim mortis metu omnis quietae vitae status perturbatur, et ut succumbere doloribus eosque humili animo inbecilloque ferre miserum est, ob eamque debilitatem animi multi parentes, multi amicos, non nulli patriam, plerique autem se ipsos penitus perdiderunt, sic robustus animus et excelsus omni est liber cura et angore, cum et mortem contemnit, qua qui affecti sunt in eadem causa sunt, qua ante quam nati, et ad dolores ita paratus est, ut meminerit maximos morte finiri, parvos multa habere intervalla requietis, mediocrium nos esse dominos, ut, si tolerabiles sint, feramus, si minus, animo aequo e vita, cum ea non placeat, tamquam e theatro exeamus. quibus rebus intellegitur nec timiditatem ignaviamque vituperari nec fortitudinem patientiamque laudari suo nomine, sed illas reici, quia dolorem pariant, has optari, quia voluptatem.   [50] Iustitia restat, ut de omni virtute sit dictum. sed similia fere dici possunt. ut enim sapientiam, temperantiam, fortitudinem copulatas esse docui cum voluptate, ut ab ea nullo modo nec divelli nec distrahi possint, sic de iustitia iudicandum est, quae non modo numquam nocet cuiquam, sed contra semper afficit cum vi sua atque natura, quod tranquillat animos, tum spe nihil earum rerum defuturum, quas natura non depravata desiderat. [et] quem ad modum temeritas et libido et ignavia semper animum excruciant et semper sollicitant turbulentaeque sunt, sic [inprobitas si] cuius in mente consedit, hoc ipso, quod adest, turbulenta est; si vero molita quippiam est, quamvis occulte fecerit, numquam tamen id confidet fore semper occultum. plerumque improborum facta primo suspicio insequitur, dein sermo atque fama, tum accusator, tum iudex; [51] multi etiam, ut te consule, ipsi se indicaverunt. quodsi qui satis sibi contra hominum conscientiam saepti esse et muniti videntur, deorum tamen horrent easque ipsas sollicitudines, quibus eorum animi noctesque diesque exeduntur, a diis inmortalibus supplicii causa importari putant. quae autem tanta ex improbis factis ad minuendas vitae molestias accessio potest fieri, quanta ad augendas, cum conscientia factorum, tum poena legum odioque civium? et tamen in quibusdam neque pecuniae modus est neque honoris neque imperii nec libidinum nec epularum nec reliquarum cupiditatum, quas nulla praeda umquam improbe parta minuit, [sed] potius inflammat, ut coercendi magis quam dedocendi esse videantur.   [52] Invitat igitur vera ratio bene sanos ad iustitiam, aequitatem, fidem, neque homini infanti aut inpotenti iniuste facta conducunt, qui nec facile efficere possit, quod conetur, nec optinere, si effecerit, et opes vel fortunae vel ingenii liberalitati magis conveniunt, qua qui utuntur, benivolentiam sibi conciliant et, quod aptissimum est ad quiete vivendum, caritatem, praesertim cum omnino nulla sit causa peccandi. [53] quae enim cupiditates a natura proficiscuntur, facile explentur sine ulla iniuria, quae autem inanes sunt, iis parendum non est. nihil enim desiderabile concupiscunt, plusque in ipsa iniuria detrimenti est quam in iis rebus emolumenti, quae pariuntur iniuria. Itaque ne iustitiam quidem recte quis dixerit per se ipsam optabilem, sed quia iucunditatis vel plurimum afferat. nam diligi et carum esse iucundum est propterea, quia tutiorem vitam et voluptatem pleniorem efficit. itaque non ob ea solum incommoda, quae eveniunt inprobis, fugiendam inprobitatem putamus, sed multo etiam magis, quod, cuius in animo versatur, numquam sinit eum respirare, numquam adquiescere.   [54] Quodsi ne ipsarum quidem virtutum laus, in qua maxime ceterorum philosophorum exultat oratio, reperire exitum potest, nisi derigatur ad voluptatem, voluptas autem est sola, quae nos vocet ad se et alliciat suapte natura, non potest esse dubium, quin id sit summum atque extremum bonorum omnium, beateque vivere nihil aliud sit nisi cum voluptate vivere.   [55] Huic certae stabilique sententiae quae sint coniuncta explicabo brevi. nullus in ipsis error est finibus bonorum et malorum, id est in voluptate aut in dolore, sed in his rebus peccant, cum e quibus haec efficiantur ignorant. animi autem voluptates et dolores nasci fatemur e corporis voluptatibus et doloribus -- itaque concedo, quod modo dicebas, cadere causa, si qui e nostris aliter existimant, quos quidem video esse multos, sed imperitos --, quamquam autem et laetitiam nobis voluptas animi et molestiam dolor afferat, eorum tamen utrumque et ortum esse e corpore et ad corpus referri, nec ob eam causam non multo maiores esse et voluptates et dolores animi quam corporis. nam corpore nihil nisi praesens et quod adest sentire possumus, animo autem et praeterita et futura. ut enim aeque doleamus animo, cum corpore dolemus, fieri tamen permagna accessio potest, si aliquod aeternum et infinitum impendere malum nobis opinemur. quod idem licet transferre in voluptatem, ut ea maior sit, si nihil tale metuamus. [ 56] Iam illud quidem perspicuum est, maximam animi aut voluptatem aut molestiam plus aut ad beatam aut ad miseram vitam afferre momenti quam eorum utrumvis, si aeque diu sit in corpore. Non placet autem detracta voluptate aegritudinem statim consequi, nisi in voluptatis locum dolor forte successerit, at contra gaudere nosmet omittendis doloribus, etiamsi voluptas ea, quae sensum moveat, nulla successerit, eoque intellegi potest quanta voluptas sit non dolere. [57] Sed ut iis bonis erigimur, quae expectamus, sic laetamur iis, quae recordamur. stulti autem malorum memoria torquentur, sapientes bona praeterita grata recordatione renovata delectant. est autem situm in nobis ut et adversa quasi perpetua oblivione obruamus et secunda iucunde ac suaviter meminerimus. sed cum ea, quae praeterierunt, acri animo et attento intuemur, tum fit ut aegritudo sequatur, si illa mala sint, laetitia, si bona.   O praeclaram beate vivendi et apertam et simplicem et directam viam! eum enim certe nihil homini possit melius esse quam vacare omni dolore et molestia perfruique maximis et animi et corporis voluptatibus, videtisne quam nihil praetermittatur quod vitam adiuvet, quo facilius id, quod propositum est, summum bonum consequamur? clamat Epicurus, is quem vos nimis voluptatibus esse deditum dicitis; non posse iucunde vivi, nisi sapienter, honeste iusteque vivatur, nec sapienter, honeste, iuste, nisi iucunde. [58] neque enim civitas in seditione beata esse potest nec in discordia dominorum domus; quo minus animus a se ipse dissidens secumque discordans gustare partem ullam liquidae voluptatis et liberae potest. atqui pugnantibus et contrariis studiis consiliisque semper utens nihil quieti videre, nihil tranquilli potest.   [59] Quodsi corporis gravioribus morbis vitae iucunditas impeditur, quanto magis animi morbis impediri necesse est! animi autem morbi sunt cupiditates inmensae et inanes divitiarum, gloriae, dominationis, libidinosarum etiam voluptatum. accedunt aegritudines, molestiae, maerores, qui exedunt animos conficiuntque curis hominum non intellegentium nihil dolendum esse animo, quod sit a dolore corporis praesenti futurove seiunctum. nec vero quisquam stultus non horum morborum aliquo laborat, nemo igitur est non miser. [60] accedit etiam mors, quae quasi saxum Tantalo semper impendet, tum superstitio, qua qui est imbutus quietus esse numquam potest. praeterea bona praeterita non meminerunt, praesentibus non fruuntur, futura modo expectant, quae quia certa esse non possunt, conficiuntur et angore et metu maximeque cruciantur, cum sero sentiunt frustra se aut pecuniae studuisse aut imperiis aut opibus aut gloriae. nullas enim consequuntur voluptates, quarum potiendi spe inflammati multos labores magnosque susceperant. [61] ecce autem alii minuti et angusti aut omnia semper desperantes aut malivoli, invidi, difficiles, lucifugi, maledici, monstruosi, alii autem etiam amatoriis levitatibus dediti, alii petulantes, alii audaces, protervi, idem intemperantes et ignavi, numquam in sententia permanentes, quas ob causas in eorum vita nulla est intercapedo molestiae. igitur neque stultorum quisquam beatus neque sapientium non beatus. Multoque hoc melius nos veriusque quam Stoici. illi enim negant esse bonum quicquam nisi nescio quam illam umbram, quod appellant honestum non tam solido quam splendido nomine, virtutem autem nixam hoc honesto nullam requirere voluptatem atque ad beate vivendum se ipsa esse contentam.   [62] Sed possunt haec quadam ratione dici non modo non repugnantibus, verum etiam approbantibus nobis. sic enim ab Epicuro sapiens semper beatus inducitur: finitas habet cupiditates, neglegit mortem, de diis inmortalibus sine ullo metu vera sentit, non dubitat, si ita melius sit, migrare de vita. his rebus instructus semper est in voluptate. neque enim tempus est ullum, quo non plus voluptatum habeat quam dolorum. nam et praeterita grate meminit et praesentibus ita potitur, ut animadvertat quanta sint ea quamque iucunda, neque pendet ex futuris, sed expectat illa, fruitur praesentibus ab iisque vitiis, quae paulo ante collegi, abest plurimum et, cum stultorum vitam cum sua comparat, magna afficitur voluptate. dolores autem si qui incurrunt, numquam vim tantam habent, ut non plus habeat sapiens, quod gaudeat, quam quod angatur. [63] optime vero Epicurus, quod exiguam dixit fortunam intervenire sapienti maximasque ab eo et gravissimas res consilio ipsius et ratione administrari neque maiorem voluptatem ex infinito tempore aetatis percipi posse, quam ex hoc percipiatur, quod videamus esse finitum. In dialectica autem vestra nullam existimavit esse nec ad melius vivendum nec ad commodius disserendum viam. In physicis plurimum posuit. ea scientia et verborum vis et natura orationis et consequentium repugnantiumve ratio potest perspici. omnium autem rerum natura cognita levamur superstitione, liberamur mortis metu, non conturbamur ignoratione rerum, e qua ipsa horribiles existunt saepe formidines. denique etiam morati melius erimus, cum didicerimus quid natura desideret. tum vero, si stabilem scientiam rerum tenebimus, servata illa, quae quasi delapsa de caelo est ad cognitionem omnium, regula, ad quam omnia iudicia rerum dirigentur, numquam ullius oratione victi sententia desistemus. [64] nisi autem rerum natura perspecta erit, nullo modo poterimus sensuum iudicia defendere. quicquid porro animo cernimus, id omne oritur a sensibus; qui si omnes veri erunt, ut Epicuri ratio docet, tum denique poterit aliquid cognosci et percipi. quos qui tollunt et nihil posse percipi dicunt, ii remotis sensibus ne id ipsum quidem expedire possunt, quod disserunt. praeterea sublata cognitione et scientia tollitur omnis ratio et vitae degendae et rerum gerendarum. sic e physicis et fortitudo sumitur contra mortis timorem et constantia contra metum religionis et sedatio animi omnium rerum occultarum ignoratione sublata et moderatio natura cupiditatum generibusque earum explicatis, et, ut modo docui, cognitionis regula et iudicio ab eadem illa constituto veri a falso distinctio traditur.  [65] Restat locus huic disputationi vel maxime necessarius de amicitia, quam, si voluptas summum sit bonum, affirmatis nullam omnino fore. de qua Epicurus quidem ita dicit, omnium rerum, quas ad beate vivendum sapientia comparaverit, nihil esse maius amicitia, nihil uberius, nihil iucundius. nec vero hoc oratione solum, sed multo magis vita et factis et moribus comprobavit. quod quam magnum sit fictae veterum fabulae declarant, in quibus tam multis tamque variis ab ultima antiquitate repetitis tria vix amicorum paria reperiuntur, ut ad Orestem pervenias profectus a Theseo. at vero Epicurus una in domo, et ea quidem angusta, quam magnos quantaque amoris conspiratione consentientis tenuit amicorum greges! quod fit etiam nunc ab Epicureis. sed ad rem redeamus; de hominibus dici non necesse est.   [66] Tribus igitur modis video esse a nostris de amicitia disputatum. alii cum eas voluptates, quae ad amicos pertinerent, negarent esse per se ipsas tam expetendas, quam nostras expeteremus, quo loco videtur quibusdam stabilitas amicitiae vacillare, tuentur tamen eum locum seque facile, ut mihi videtur, expediunt. ut enim virtutes, de quibus ante dictum est, sic amicitiam negant posse a voluptate discedere. nam cum solitudo et vita sine amicis insidiarum et metus plena sit, ratio ipsa monet amicitias comparare, quibus partis confirmatur animus et a spe pariendarum voluptatum seiungi non potest. [67] atque ut odia, invidiae, despicationes adversantur voluptatibus, sic amicitiae non modo fautrices fidelissimae, sed etiam effectrices sunt voluptatum tam amicis quam sibi, quibus non solum praesentibus fruuntur, sed etiam spe eriguntur consequentis ac posteri temporis. quod quia nullo modo sine amicitia firmam et perpetuam iucunditatem vitae tenere possumus neque vero ipsam amicitiam tueri, nisi aeque amicos et nosmet ipsos diligamus, idcirco et hoc ipsum efficitur in amicitia, et amicitia cum voluptate conectitur. nam et laetamur amicorum laetitia aeque atque nostra et pariter dolemus angoribus. [68] quocirca eodem modo sapiens erit affectus erga amicum, quo in se ipsum, quosque labores propter suam voluptatem susciperet, eosdem suscipiet propter amici voluptatem. quaeque de virtutibus dicta sunt, quem ad modum eae semper voluptatibus inhaererent, eadem de amicitia dicenda sunt. praeclare enim Epicurus his paene verbis: 'Eadem', inquit, 'scientia confirmavit animum, ne quod aut sempiternum aut diuturnum timeret malum, quae perspexit in hoc ipso vitae spatio amicitiae praesidium esse firmissimum.'   [69] Sunt autem quidam Epicurei timidiores paulo contra vestra convicia, sed tamen satis acuti, qui verentur ne, si amicitiam propter nostram voluptatem expetendam putemus, tota amicitia quasi claudicare videatur. itaque primos congressus copulationesque et consuetudinum instituendarum voluntates fieri propter voluptatem; cum autem usus progrediens familiaritatem effecerit, tum amorem efflorescere tantum, ut, etiamsi nulla sit utilitas ex amicitia, tamen ipsi amici propter se ipsos amentur. etenim si loca, si fana, si urbes, si gymnasia, si campum, si canes, si equos, si ludicra exercendi aut venandi consuetudine adamare solemus, quanto id in hominum consuetudine facilius fieri poterit et iustius?   [70] Sunt autem, qui dicant foedus esse quoddam sapientium, ut ne minus amicos quam se ipsos diligant. quod et posse fieri intellegimus et saepe etiam videmus, et perspicuum est nihil ad iucunde vivendum reperiri posse, quod coniunctione tali sit aptius. Quibus ex omnibus iudicari potest non modo non impediri rationem amicitiae, si summum bonum in voluptate ponatur, sed sine hoc institutionem omnino amicitiae non posse reperiri.   [71] Quapropter si ea, quae dixi, sole ipso illustriora et clariora sunt, si omnia dixi hausta e fonte naturae, si tota oratio nostra omnem sibi fidem sensibus confirmat, id est incorruptis atque integris testibus, si infantes pueri, mutae etiam bestiae paene loquuntur magistra ac duce natura nihil esse prosperum nisi voluptatem, nihil asperum nisi dolorem, de quibus neque depravate iudicant neque corrupte, nonne ei maximam gratiam habere debemus, qui hac exaudita quasi voce naturae sic eam firme graviterque comprehenderit, ut omnes bene sanos in viam placatae, tranquillae, quietae, beatae vitae deduceret? Qui quod tibi parum videtur eruditus, ea causa est, quod nullam eruditionem esse duxit, nisi quae beatae vitae disciplinam iuvaret. [72] an ille tempus aut in poetis evolvendis, ut ego et Triarius te hortatore facimus, consumeret, in quibus nulla solida utilitas omnisque puerilis est delectatio, aut se, ut Plato, in musicis, geometria, numeris, astris contereret, quae et a falsis initiis profecta vera esse non possunt et, si essent vera, nihil afferrent, quo iucundius, id est quo melius viveremus, eas ergo artes persequeretur, vivendi artem tantam tamque et operosam et perinde fructuosam relinqueret? non ergo Epicurus ineruditus, sed ii indocti, qui, quae pueros non didicisse turpe est, ea putant usque ad senectutem esse discenda.   Quae cum dixisset, Explicavi, inquit, sententiam meam, et eo quidem consilio, tuum iudicium ut cognoscerem, quoniam mihi ea facultas, ut id meo arbitratu facerem, ante hoc tempus numquam est data. ";
     private boolean textType;
 
     /**
      * Constructor
      *
      * @param id               internal id
      * @param primitiveValueType if the value type is a primitive (int, long, ...) or a String
      * @param singleRowStorage can the underlying data be stored in a single db row?
      * @param textType         is this a text type? text types will be fulltext indexed as default
      * @param valueClass       class of the value object
      */
     FxDataType(long id, boolean primitiveValueType, boolean singleRowStorage, boolean textType, Class<? extends FxValue> valueClass) {
         this.initialized = false;
         this.id = id;
         this.primitiveValueType = primitiveValueType;
         this.singleRowStorage = singleRowStorage;
         this.textType = textType;
         this.valueClass = valueClass;
     }
 
 
     /**
      * Get the internal id
      *
      * @return internal id
      */
     public long getId() {
         return id;
     }
 
     /**
      * Get the name of this data type
      *
      * @return name of this data type
      */
     public String getName() {
         if (!initialized)
             return this.name();
         return name;
     }
 
     /**
      * Get the localized label of this data type
      *
      * @return localized label of this data type
      */
     public FxString getLabel() {
         if (!initialized)
             return new FxString("unknown");
         return label;
     }
 
     /**
      * Return true if this data type maps to a primitive Java value type (int, long, ...) or a String.
      *
      * @return  true if this data type maps to a primitive Java value type (int, long, ...) or a String.
      * @since 3.1
      */
     public boolean isPrimitiveValueType() {
         return primitiveValueType;
     }
 
     /**
      * Is this data type initialized yet (name and label known)?
      *
      * @return data type initialized yet (name and label known)?
      */
     public boolean isInitialized() {
         return initialized;
     }
 
     /**
      * Initialized this data type by assigning the name and label as
      * stored in the database
      *
      * @param name  name
      * @param label label
      */
     public void initialize(String name, FxString label) {
         if (initialized)
             return;
         this.name = name;
         this.label = label;
         initialized = true;
     }
 
     /**
      * Low-level validation of values. Checks if the passed value may be stored
      * in the database, but does not check other requirements which may be imposed through
      * the property definition.
      *
      * @param value the value to be validated
      * @return true if the given value is a valid representation for the datatype, false otherwise
      */
     public boolean isValid(Object value) {
         if (value instanceof FxNoAccess)
             return true;
         if (value instanceof FxValue) {
             if (!((FxValue) value).isValid()) {
                 return false;
             }
         }
         switch (this) {
             case String1024:
                 final FxString fxString = (FxString) value;
                 final String defaultTrans = fxString.getDefaultTranslation();
                 return value instanceof FxString &&
                         (fxString.isEmpty() || (defaultTrans != null && defaultTrans.length() <= 1024));
             case Binary:
                 return value instanceof FxBinary;
             case Boolean:
                 return value instanceof FxBoolean;
             case Date:
                 return value instanceof FxDate;
             case DateTime:
                 return value instanceof FxDateTime;
             case DateRange:
                 return value instanceof FxDateRange;
             case DateTimeRange:
                 return value instanceof FxDateTimeRange;
             case Double:
                 return value instanceof FxDouble;
             case Float:
                 return value instanceof FxFloat;
             case HTML:
                 return value instanceof FxHTML;
             case LargeNumber:
                 return value instanceof FxLargeNumber;
             case Number:
                 return value instanceof FxNumber;
             case Reference:
                 return value instanceof FxReference;
             case Text:
                 return value instanceof FxString;
             case SelectOne:
             case SelectMany:
             case InlineReference:
             default:
                 // TODO: implement missing data types
                 return true;
         }
     }
 
     /**
      * Can this data type be stored in a single (database) row?
      *
      * @return storage possible in a single (database) row
      */
     public boolean isSingleRowStorage() {
         return singleRowStorage;
     }
 
     /**
      * Can this data type potentially contain text?
      * This flag determines if a property/assignment should be fulltext indexed by default.
      *
      * @return data type can potentially contain text
      */
     public boolean isTextType() {
         return textType;
     }
 
     /**
      * Get an random FxValue instance for the assignments multilanguage and selectlist, etc. settings
      *
      * @param rnd        random
      * @param assignment assignment
      * @return random FxValue instance for the data type
      */
     public FxValue getRandomValue(Random rnd, FxPropertyAssignment assignment) {
         switch (this) {
             case HTML:
                 return new FxHTML(getRandomString(assignment.isMultiLang(), rnd, 2048));
             case String1024:
                 return new FxString(getRandomString(assignment.isMultiLang(), rnd, 1024));
             case Text:
                 return new FxString(getRandomString(assignment.isMultiLang(), rnd, 4096));
             case Number:
                 return new FxNumber(assignment.isMultiLang(), rnd.nextInt());
             case LargeNumber:
                 return new FxLargeNumber(assignment.isMultiLang(), rnd.nextLong());
             case Float:
                 return new FxFloat(assignment.isMultiLang(), rnd.nextFloat());
             case Double:
                 return new FxDouble(assignment.isMultiLang(), rnd.nextDouble());
             case Date:
                 return new FxDate(assignment.isMultiLang(), getRandomDate(rnd));
             case DateTime:
                 return new FxDateTime(assignment.isMultiLang(), getRandomDate(rnd));
             case DateRange:
                 final List<Date> dates = Arrays.asList(getRandomDate(rnd), getRandomDate(rnd));
                 return new FxDateRange(assignment.isMultiLang(), new DateRange(Collections.min(dates), Collections.max(dates)));
             case DateTimeRange:
                 final List<Date> dates2 = Arrays.asList(getRandomDate(rnd), getRandomDate(rnd));
                 return new FxDateTimeRange(assignment.isMultiLang(), new DateRange(Collections.min(dates2), Collections.max(dates2)));
             case Boolean:
                 return new FxBoolean(assignment.isMultiLang(), rnd.nextBoolean());
             case Binary:
                 return new FxBinary(assignment.isMultiLang(), FxBinary.EMPTY);
             case Reference:
                 return new FxReference(assignment.isMultiLang(), FxReference.EMPTY);
             case SelectOne:
                 final List<FxSelectListItem> items = assignment.getProperty().getReferencedList().getItems();
                 return new FxSelectOne(assignment.isMultiLang(), items.get(rnd.nextInt(items.size())));
             case SelectMany:
                 final SelectMany many = new SelectMany(assignment.getProperty().getReferencedList());
                 final List<FxSelectListItem> available = new ArrayList<FxSelectListItem>(many.getAvailable());
                 for (FxSelectListItem item : available) {
                     if (rnd.nextInt() % 2 == 0) {
                         many.selectItem(item);
                     }
                 }
                 return new FxSelectMany(assignment.isMultiLang(), many);
             case InlineReference:
             default:
                 throw new FxNotFoundException("ex.structure.datatype.notImplemented", this.name()).asRuntimeException();
         }
     }
 
     /**
      * Get an random FxValue instance for the data type
      *
      * @param rnd       random
      * @param multilang multilanguage?
      * @return random FxValue instance for the data type
      */
     public FxValue getRandomValue(Random rnd, boolean multilang) {
         switch (this) {
             case HTML:
                 return new FxHTML(getRandomString(multilang, rnd, 2048));
             case String1024:
                 return new FxString(getRandomString(multilang, rnd, 1024));
             case Text:
                 return new FxString(getRandomString(multilang, rnd, 4096));
             case Number:
                 return new FxNumber(multilang, rnd.nextInt());
             case LargeNumber:
                 return new FxLargeNumber(multilang, rnd.nextLong());
             case Float:
                 return new FxFloat(multilang, rnd.nextFloat());
             case Double:
                 return new FxDouble(multilang, rnd.nextDouble());
             case Date:
                 //make sure time is stripped
                 return new FxDate(multilang, FxFormatUtils.toDate(FxFormatUtils.getDateFormat().format(getRandomDate(rnd))));
             case DateTime:
                 return new FxDateTime(multilang, getRandomDate(rnd));
             case DateRange:
                 //make sure time is stripped
                 final List<Date> dates = Arrays.asList(
                         FxFormatUtils.toDate(FxFormatUtils.getDateFormat().format(getRandomDate(rnd))),
                         FxFormatUtils.toDate(FxFormatUtils.getDateFormat().format(getRandomDate(rnd))));
                 return new FxDateRange(multilang, new DateRange(Collections.min(dates), Collections.max(dates)));
             case DateTimeRange:
                 final List<Date> dates2 = Arrays.asList(getRandomDate(rnd), getRandomDate(rnd));
                 return new FxDateTimeRange(multilang, new DateRange(Collections.min(dates2), Collections.max(dates2)));
             case Boolean:
                 return new FxBoolean(multilang, rnd.nextBoolean());
             case Binary:
                 return new FxBinary(multilang, new BinaryDescriptor(getRandomText(rnd, 15), getRandomText(rnd, 8), 0, "application/binary", ""));
             case Reference:
                 return new FxReference(multilang, FxReference.EMPTY);
             case SelectOne:
             case SelectMany:
             case InlineReference:
             default:
                 throw new FxNotFoundException("ex.structure.datatype.notImplemented", this.name()).asRuntimeException();
         }
     }
 
     private Date getRandomDate(Random rnd) {
         final Calendar cal = Calendar.getInstance();
        cal.set(rnd.nextInt(3000), rnd.nextInt(12), 1 + rnd.nextInt(28));
         return cal.getTime();
     }
 
     /**
      * Returns the concrete {@link FxValue} implementation class for values of this
      * datatype.
      *
      * @return the concrete {@link FxValue} implementation class for values of this
      *         datatype.
      */
     public Class<? extends FxValue> getValueClass() {
         return valueClass;
     }
 
     /**
      * Get a random String
      *
      * @param multilang multi language?
      * @param rnd       Random to use
      * @param maxLength max. maxLength
      * @return random string
      */
     private FxString getRandomString(boolean multilang, Random rnd, int maxLength) {
         FxString ret = new FxString(multilang, FxLanguage.ENGLISH, getRandomText(rnd, maxLength));
         if (multilang) {
             ret.setTranslation(FxLanguage.GERMAN, getRandomText(rnd, maxLength));
             if (rnd.nextBoolean())
                 ret.setTranslation(FxLanguage.FRENCH, getRandomText(rnd, maxLength));
             if (rnd.nextBoolean())
                 ret.setTranslation(FxLanguage.ITALIAN, getRandomText(rnd, maxLength));
         }
         return ret;
     }
 
     /**
      * Get a random text
      *
      * @param rnd       Random to use
      * @param maxLength max. length
      * @return random text
      */
     private String getRandomText(Random rnd, int maxLength) {
         int start = rnd.nextInt(RANDOM_TEXT.length() - maxLength);
         int len = Math.max(15, rnd.nextInt(Math.min(RANDOM_TEXT.length() - start, maxLength)));
         if (len > maxLength)
             len = maxLength;
         return RANDOM_TEXT.substring(start, start + len);
     }
 }
