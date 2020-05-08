 /**
  * 
  */
 package pt.carlos.alex.rosario;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.util.Log;
 
 
 /**
  * @author Alexandre Carlos
  * 
  */
 public final class Misterios {
 
 	private static final String TAG="Misterios";
 //	private static final boolean DEBUG=true;
 	
 	public static final String[] DESIGN = { "Mistérios Gloriosos (da Glória)", // 0
 			"Mistérios Gozosos (da Alegria)", // 1
 			"Mistérios Dolorosos (da Dor)", // 2
 			"Mistérios Luminosos (da Luz)" // 3
 	};
 
 	public final static int[] MISTERIOS = { -1, // Erro ???
 			0, // Domingo - Mistérios Gloriosos (da Glória)
 			1, // 2ª Feira - Mistérios Gozosos (da Alegria)
 			2, // 3ª Feira - Mistérios Dolorosos (da Dor)
 			0, // 4ª Feira - Mistérios Gloriosos (da Glória)
 			3, // 5ª Feira - Mistérios Luminosos (da Luz)
 			2, // 6ª Feira - Mistérios Dolorosos (da Dor)
 			1 // Sábado - Mistérios Gozosos (da Alegria)
 	};
 
 	public final static String[] GLORIOSOS = {
 			"1º Mistério - A ressureição de Nosso Senhor Jesus Cristo",
 			"2º Mistério - A Ascensão de Jesus ao Céu",
 			"3º Mistério - A descida do Espírito Santo sobre Nossa Senhora e os Apóstolos",
 			"4º Mistério - A Assunção de Nossa Senhora ao Céu",
 			"5º Mistério - A Coroação de Nossa Senhora, no Céu, como Rainha dos Anjos e dos Santos",
 			"Orações finais" };
 
 	public final static String[] GOZOSOS = {
 			"1º Mistério - Anunciação do Anjo a Nossa Senhora",
 			"2º Mistério - A visita de Nossa Senhora a Sua prima Santa Isabel",
 			"3º Mistério - Nascimento de Jesus em Belém",
 			"4º Mistério - Apresentação de Jesus no Templo",
 			"5º Mistério - Jesus no Templo, entre os Doutores",
 			"Orações finais" };
 
 	public final static String[] DOLOROSOS = {
 			"1º Mistério - Agonia de Jesus no Horto das Oliveiras",
 			"2º Mistério - A flagelação de Jesus",
 			"3º Mistério - A coroação de espinhos",
 			"4º Mistério - Jesus a caminho do Calvário com a cruz às costas",
 			"5º Mistério - A crucificação e morte de Jesus", "Orações finais" };
 
 	public final static String[] LUMINOSOS = {
 			"1º Mistério - O Batismo de Jesus no rio Jordão",
 			"2º Mistério - A revelação de Jesus nas bodas de Caná",
 			"3º Mistério - O Anúncio do Reino de Deus e o convite à conversão",
 			"4º Mistério - A Transfiguração de Jesus",
 			"5º Mistério - A Última Ceia de Jesus e a instituição da Eucarístia",
 			"Orações finais" };
 
 	// private static final String PRIMEIRO_GLORIOSO =
 	// "<html><head></head><body>1º Mistério - A ressureição de Nosso Senhor Jesus Cristo</body></html>";
 	//
 	// private static final String SEGUNDO_GLORIOSO =
 	// "<html><head></head><body>2º Mistério - A Ascensão de Jesus ao Céu</body></html>";
 	//
 	// private static final String TERCEIRO_GLORIOSO =
 	// "<html><head></head><body>3º Mistério - A descida do Espírito Santo sobre Nossa Senhora e os Apóstolos</body></html>";
 	//
 	// private static final String QUARTO_GLORIOSO =
 	// "<html><head></head><body>4º Mistério - A Assunção de Nossa Senhora ao Céu</body></html>";
 	//
 	// private static final String QUINTO_GLORIOSO =
 	// "<html><head></head><body>5º Mistério - A Coroação de Nossa Senhora, no Céu, como Rainha dos Anjos e dos Santos</body></html>";
 	//
 	// private static final String PRIMEIRO_GOZOSO =
 	// "<html><head></head><body><div align=\"center\"><h1><font color=\"#0000FF\">1º Mistério Gozoso</font></h1><h2><font color=\"#0000FF\">Anúncio do nascimento de Jesus <sub><i>(Lc 1,26-38)</i></sub></font></h2></div><br /><sup><font color=\"#A52A2A\">26</font></sup>Ao sexto mês, o anjo Gabriel foi enviado por Deus a uma cidade da Galileia chamada Nazaré, <br /><sup><font color=\"#A52A2A\">27</font></sup>a uma virgem desposada com um homem chamado José, da casa de David; e o nome da virgem era Maria.<br /><sup><font color=\"#A52A2A\">28</font></sup>Ao entrar em casa dela, o anjo disse-lhe: «Salve, ó cheia de graça, o Senhor está contigo.» <br /><sup><font color=\"#A52A2A\">29</font></sup>Ao ouvir estas palavras, ela perturbou-se e inquiria de si própria o que significava tal saudação. <br /><sup><font color=\"#A52A2A\">30</font></sup>Disse-lhe o anjo: «Maria, não temas, pois achaste graça diante de Deus. <br /><sup><font color=\"#A52A2A\">31</font></sup>Hás-de conceber no teu seio e dar à luz um filho, ao qual porás o nome de Jesus. <br /><sup><font color=\"#A52A2A\">32</font></sup>Será grande e vai chamar-se Filho do Altíssimo. O Senhor Deus vai dar-lhe o trono de seu pai David, <br /><sup><font color=\"#A52A2A\">33</font></sup>reinará eternamente sobre a casa de Jacob e o seu reinado não terá fim.»<br /><sup><font color=\"#A52A2A\">34</font></sup>Maria disse ao anjo: «Como será isso, se eu não conheço homem?» <br /><sup><font color=\"#A52A2A\">35</font></sup>O anjo respondeu-lhe: «O Espírito Santo virá sobre ti e a força do Altíssimo estenderá sobre ti a sua sombra. Por isso, aquele que vai nascer é Santo e será chamado Filho de Deus. <br /><sup><font color=\"#A52A2A\">36</font></sup>Também a tua parente Isabel concebeu um filho na sua velhice e já está no sexto mês, ela, a quem chamavam estéril, <br /><sup><font color=\"#A52A2A\">37</font></sup>porque nada é impossível a Deus.» <br /><sup><font color=\"#A52A2A\">38</font></sup>Maria disse, então: «Eis a serva do Senhor, faça-se em mim segundo a tua palavra.» E o anjo retirou-se de junto dela. </body></html>";
 	//
 	// private static final String SEGUNDO_GOZOSO =
 	// "<html><head></head><body><h1><font color=\"#0000FF\">2º Mistério Gozoso</font></h1><h2><font color=\"#0000FF\">A visita de Nossa Senhora a Sua prima Santa Isabel <sub><i>(Lc 1, 39-45)</i></sub></font></h2><br /><sup><font color=\"#A52A2A\">39</font></sup>Por aqueles dias, Maria pôs-se a caminho e dirigiu-se à pressa para a montanha, a uma cidade da Judeia. <br /><sup><font color=\"#A52A2A\">40</font></sup>Entrou em casa de Zacarias e saudou Isabel. <br /><sup><font color=\"#A52A2A\">41</font></sup>Quando Isabel ouviu a saudação de Maria, o menino saltou-lhe de alegria no seio e Isabel ficou cheia do Espírito Santo. <br /><sup><font color=\"#A52A2A\">42</font></sup>Então, erguendo a voz, exclamou: «Bendita és tu entre as mulheres e bendito é o fruto do teu ventre. <br /><sup><font color=\"#A52A2A\">43</font></sup>E donde me é dado que venha ter comigo a mãe do meu Senhor? <br /><sup><font color=\"#A52A2A\">44</font></sup>Pois, logo que chegou aos meus ouvidos a tua saudação, o menino saltou de alegria no meu seio. <br /><sup><font color=\"#A52A2A\">45</font></sup>Feliz de ti que acreditaste, porque se vai cumprir tudo o que te foi dito da parte do Senhor.» </body></html>";
 	//
 	// private static final String TERCEIRO_GOZOSO =
 	// "<html><head></head><body><h1><font color=\"#0000FF\">3º Mistério Gozoso</font></h1><h2><font color=\"#0000FF\">Nascimento de Jesus em Belém <sub><i>(Lc 2,4-7)</i></sub></font></h2><br /><sup><font color=\"#A52A2A\">4</font></sup>Também José, deixando a cidade de Nazaré, na Galileia, subiu até à Judeia, à cidade de David, chamada Belém, por ser da casa e linhagem de David, <br /><sup><font color=\"#A52A2A\">5</font></sup>a fim de se recensear com Maria, sua esposa, que se encontrava grávida.<br /><sup><font color=\"#A52A2A\">6</font></sup>E, quando eles ali se encontravam, completaram-se os dias de ela dar à luz <br /><sup><font color=\"#A52A2A\">7</font></sup>e teve o seu filho primogénito, que envolveu em panos e recostou numa manjedoura, por não haver lugar para eles na hospedaria. </body></html>";
 	//
 	// private static final String QUARTO_GOZOSO =
 	// "<html><head></head><body><h1><font color=\"#0000FF\">4º Mistério Gozoso</font></h1><h2><font color=\"#0000FF\">Apresentação de Jesus no Templo <sub><i>(Lc 2,22-24)</i></sub></font></h2><br /><sup><font color=\"#A52A2A\">22</font></sup>Quando se cumpriu o tempo da sua purificação, segundo a Lei de Moisés, levaram-no a Jerusalém para o apresentarem ao Senhor, <br /><sup><font color=\"#A52A2A\">23</font></sup>conforme está escrito na Lei do Senhor: «Todo o primogénito varão será consagrado ao Senhor» <br /><sup><font color=\"#A52A2A\">24</font></sup>e para oferecerem em sacrifício, como se diz na Lei do Senhor, duas rolas ou duas pombas. </body></html>";
 	//
 	// private static final String QUINTO_GOZOSO =
 	// "<html><head></head><body><h1><font color=\"#0000FF\">5º Mistério Gozoso</font></h1><h2><font color=\"#0000FF\">Jesus no Templo, entre os Doutores <sub>(Lc 2,41-51)</sub></font></h2><br /><sup><font color=\"#A52A2A\">41</font></sup>Os pais de Jesus iam todos os anos a Jerusalém, pela festa da Páscoa. <br /><sup><font color=\"#A52A2A\">42</font></sup>Quando Ele chegou aos doze anos, subiram até lá, segundo o costume da festa. <br /><sup><font color=\"#A52A2A\">43</font></sup>Terminados esses dias, regressaram a casa e o menino ficou em Jerusalém, sem que os pais o soubessem. <br /><sup><font color=\"#A52A2A\">44</font></sup>Pensando que Ele se encontrava na caravana, fizeram um dia de viagem e começaram a procurá-lo entre os parentes e conhecidos. <br /><sup><font color=\"#A52A2A\">45</font></sup>Não o tendo encontrado, voltaram a Jerusalém, à sua procura.<br /><sup><font color=\"#A52A2A\">46</font></sup>Três dias depois, encontraram-no no templo, sentado entre os doutores, a ouvi-los e a fazer-lhes perguntas. <br /><sup><font color=\"#A52A2A\">47</font></sup>Todos quantos o ouviam, estavam estupefactos com a sua inteligência e as suas respostas.<br /><sup><font color=\"#A52A2A\">48</font></sup>Ao vê-lo, ficaram assombrados e sua mãe disse-lhe: «Filho, porque nos fizeste isto? Olha que teu pai e eu andávamos aflitos à tua procura!» <br /><sup><font color=\"#A52A2A\">49</font></sup>Ele respondeu-lhes: «Porque me procuráveis? Não sabíeis que devia estar em casa de meu Pai?»<br /><sup><font color=\"#A52A2A\">50</font></sup>Mas eles não compreenderam as palavras que lhes disse.<br /><sup><font color=\"#A52A2A\">51</font></sup>Depois desceu com eles, voltou para Nazaré e era-lhes submisso. Sua mãe guardava todas estas coisas no seu coração.</body></html>";
 	//
 	// private static final String PRIMEIRO_DOLOROSO =
 	// "<html><head></head><body>1º Mistério - Agonia de Jesus no Horto das Oliveiras</body></html>";
 	//
 	// private static final String SEGUNDO_DOLOROSO =
 	// "<html><head></head><body>2º Mistério - A flagelação de Jesus</body></html>";
 	//
 	// private static final String TERCEIRO_DOLOROSO =
 	// "<html><head></head><body>3º Mistério - A coroação de espinhos</body></html>";
 	//
 	// private static final String QUARTO_DOLOROSO =
 	// "<html><head></head><body>4º Mistério - Jesus a caminho do Calvário com a cruz às costas</body></html>";
 	//
 	// private static final String QUINTO_DOLOROSO =
 	// "<html><head></head><body>5º Mistério - A crucificação e morte de Jesus</body></html>";
 	//
 	// private static final String PRIMEIRO_LUMINOSO =
 	// "<html><head></head><body>1º Mistério - O Batismo de Jesus no rio Jordão</body></html>";
 	//
 	// private static final String SEGUNDO_LUMINOSO =
 	// "<html><head></head><body>2º Mistério - A revelação de Jesus nas bodas de Caná</body></html>";
 	//
 	// private static final String TERCEIRO_LUMINOSO =
 	// "<html><head></head><body>3º Mistério - O Anúncio do Reino de Deus e o convite à conversão</body></html>";
 	//
 	// private static final String QUARTO_LUMINOSO =
 	// "<html><head></head><body>4º Mistério - A Transfiguração de Jesus</body></html>";
 	//
 	// private static final String QUINTO_LUMINOSO =
 	// "<html><head></head><body>5º Mistério - A Última Ceia de Jesus e a instituição da Eucarístia</body></html>";
 	//
 	// private static final String ORACOES_FINAIS =
 	// "<html><head></head><body>Louvado seja Nosso Senhor Jesus Cristo<br />Para sempre seja louvado<br />com Sua mãe Maria Santíssima.</body></html>";
 
 	private static final String AVE_MARIA = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Avé Maria</title><meta name=\"generator\" content=\"Bluefish 2.2.2\" ><meta name=\"author\" content=\"Alexandre Carlos\" ><meta name=\"date\" content=\"2012-10-08T12:34:01+0100\" ><meta name=\"copyright\" content=\"\"><meta name=\"keywords\" content=\"\"><meta name=\"description\" content=\"\"><meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\"><meta http-equiv=\"content-style-type\" content=\"text/css\"><meta http-equiv=\"expires\" content=\"0\"><meta ><meta ></head><body><h1 style=\"text-align: center; color: #0000FF;\">Avé Maria</h1>Ave Maria,<br />cheia de graça,<br />o Senhor é convosco,<br />bendita sois Vós entre as mulheres,<br />bendito é o fruto em Vosso ventre,<br />Jesus.<br /><br />Santa Maria Mãe de Deus,<br />rogai por nós os pecadores,<br />agora e na hora da nossa morte.<br /><br />Amen<br /></body></html>";
 	private static final String DOLOROSO1 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>1º Mistério Doloroso</h1><h2>A agonia de Jesus no Horto das Oliveiras<sub><font size=\"-1\"><i>(Lc 22,39-46))</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">39</font></sup>Saiu então e foi, como de costume, para o Monte das Oliveiras. E os discípulos seguiram também com Ele. <sup><font color=\"#AA66CC\" size=\"-1\">40</font></sup>Quando chegou ao local, disse-lhes: «Orai, para que não entreis em tentação.» <sup><font color=\"#AA66CC\" size=\"-1\">41</font></sup>Depois afastou-se deles, à distância de um tiro de pedra, aproximadamente; e, pondo-se de joelhos, começou a orar, dizendo: <sup><font color=\"#AA66CC\" size=\"-1\">42</font></sup>«Pai, se quiseres, afasta de mim este cálice; contudo, não se faça a minha vontade, mas a tua.»<br><br><sup><font color=\"#AA66CC\" size=\"-1\">43</font></sup>Então, vindo do Céu, apareceu-lhe um anjo que o confortava. <sup><font color=\"#AA66CC\" size=\"-1\">44</font></sup>Cheio de angústia, pôs-se a orar mais instantemente, e o suor tornou-se-lhe como grossas gotas de sangue, que caíam na terra. <sup><font color=\"#AA66CC\" size=\"-1\">45</font></sup>Depois de orar, levantou-se e foi ter com os discípulos, encontrando-os a dormir, devido à tristeza. <sup><font color=\"#AA66CC\" size=\"-1\">46</font></sup>Disse-lhes: «Porque dormis? Levantai-vos e orai, para que não entreis em tentação.»<br></body></html>";
 	private static final String DOLOROSO2 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>2º Mistério Doloroso</h1><h2>A flagelação de Jesus<sub><font size=\"-1\"><i>(Mt 27,22-26)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">22</font></sup>Pilatos disse-lhes: «Que hei-de fazer, então, de Jesus chamado Cristo?» Todos responderam: «Seja crucificado!» <sup><font color=\"#AA66CC\" size=\"-1\">23</font></sup>Pilatos insistiu: «Que mal fez Ele?» Mas eles cada vez gritavam mais: «Seja crucificado!»<br><br><sup><font color=\"#AA66CC\" size=\"-1\">24</font></sup>Pilatos, vendo que nada conseguia e que o tumulto aumentava cada vez mais, mandou vir água e lavou as mãos na presença da multidão, dizendo: «Estou inocente deste sangue. Isso é convosco.» <sup><font color=\"#AA66CC\" size=\"-1\">25</font></sup>E todo o povo respondeu: «Que o seu sangue caia sobre nós e sobre os nossos filhos!» <sup><font color=\"#AA66CC\" size=\"-1\">26</font></sup>Então, soltou-lhes Barrabás. Quanto a Jesus, depois de o mandar flagelar, entregou-o para ser crucificado. <br></body></html>";
 	private static final String DOLOROSO3 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>3º Mistério Doloroso</h1><h2>A coroação de espinhos <sub><font size=\"-1\"><i>(Mt 27,27-31)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">27</font></sup>Os soldados do governador conduziram Jesus para o pretório e reuniram toda a coorte à volta dele. <sup><font color=\"#AA66CC\" size=\"-1\">28</font></sup>Despiram-no e envolveram-no com um manto escarlate. <sup><font color=\"#AA66CC\" size=\"-1\">29</font></sup>Tecendo uma coroa de espinhos, puseram-lha na cabeça, e uma cana na mão direita. Dobrando o joelho diante dele, escarneciam-no, dizendo: «Salve! Rei dos Judeus!» <sup><font color=\"#AA66CC\" size=\"-1\">30</font></sup>E, cuspindo-lhe no rosto, agarravam na cana e batiam-lhe na cabeça. <sup><font color=\"#AA66CC\" size=\"-1\">31</font></sup>Depois de o terem escarnecido, tiraram-lhe o manto, vestiram-lhe as suas roupas e levaram-no para ser crucificado. <br></body></html>";
 	private static final String DOLOROSO4 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>4º Mistério Doloroso</h1><h2>Jesus a caminho do Calvário <sub><font size=\"-1\"><i>(Lc 23,26-32)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">26</font></sup>Quando o iam conduzindo, lançaram mão de um certo Simão de Cirene, que voltava do campo, e carregaram-no com a cruz, para a levar atrás de Jesus.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">27</font></sup>Seguiam Jesus uma grande multidão de povo e umas mulheres que batiam no peito e se lamentavam por Ele. <sup><font color=\"#AA66CC\" size=\"-1\">28</font></sup>Jesus voltou-se para elas e disse-lhes: «Filhas de Jerusalém, não choreis por mim, chorai antes por vós mesmas e pelos vossos filhos; <sup><font color=\"#AA66CC\" size=\"-1\">29</font></sup>pois virão dias em que se dirá: ‘Felizes as estéreis, os ventres que não geraram e os peitos que não amamentaram.’ <sup><font color=\"#AA66CC\" size=\"-1\">30</font></sup>Hão-de, então, dizer aos montes: ‘Caí sobre nós!’ E às colinas: ‘Cobri-nos!’ <sup><font color=\"#AA66CC\" size=\"-1\">31</font></sup>Porque, se tratam assim a árvore verde, o que não acontecerá à seca?» <sup><font color=\"#AA66CC\" size=\"-1\">32</font></sup>E levavam também dois malfeitores, para serem executados com Ele. <br></body></html>";
 	private static final String DOLOROSO5 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>5º Mistério Doloroso</h1><h2>Crucificação e morte de Jesus <sub><i><font size=\"-1\">(Lc 23,33.44-49)</font></i></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">33</font></sup>Quando chegaram ao lugar chamado Calvário, crucificaram-no a Ele e aos malfeitores, um à direita e outro à esquerda. <br><sup><font color=\"#AA66CC\" size=\"-1\">44</font></sup>Por volta do meio-dia, as trevas cobriram toda a região até às três horas da tarde. <sup><font color=\"#AA66CC\" size=\"-1\">45</font></sup>O Sol tinha-se eclipsado e o véu do templo rasgou-se ao meio.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">46</font></sup>Dando um forte grito, Jesus exclamou: «Pai, nas tuas mãos entrego o meu espírito.» Dito isto, expirou.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">47</font></sup>Ao ver o que se passava, o centurião deu glória a Deus, dizendo: «Verdadeiramente, este homem era justo!» <sup><font color=\"#AA66CC\" size=\"-1\">48</font></sup>E toda a multidão que se tinha aglomerado para este espectáculo, vendo o que acontecera, regressava batendo no peito. <sup><font color=\"#AA66CC\" size=\"-1\">49</font></sup>Todos os seus conhecidos e as mulheres que o tinham acompanhado desde a Galileia mantinham-se à distância, observando estas coisas.<br></body></html>";
 	private static final String GLORIA = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Glória</title><meta name=\"generator\" content=\"Bluefish 2.2.2\" ><meta name=\"author\" content=\"Alexandre Carlos\" ><meta name=\"date\" content=\"2012-10-08T12:34:01+0100\" ><meta name=\"copyright\" content=\"\"><meta name=\"keywords\" content=\"\"><meta name=\"description\" content=\"\"><meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\"><meta http-equiv=\"content-style-type\" content=\"text/css\"><meta http-equiv=\"expires\" content=\"0\"><meta ><meta ></head><body><h1 style=\"text-align: center; color: #0000FF;\">Glória</h1>Glória ao Pai,<br />e ao Filho e ao Espírito Santo,<br />assim como era no princípio,<br />agora e sempre.<br /><br />Amen<br /></body></html>";
 	private static final String GLORIOSO1 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>1º Mistério Glorioso</h1><h2>A ressureição de Jesus <sub><font size=\"-1\"><i>(Lc 24,1-6)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">1</font></sup>No primeiro dia da semana, ao romper da alva, as mulheres foram ao sepulcro, levando os perfumes que haviam preparado. <sup><font color=\"#AA66CC\" size=\"-1\">2</font></sup>Encontraram removida a pedra da porta do sepulcro <sup><font color=\"#AA66CC\" size=\"-1\">3</font></sup>e, entrando, não acharam o corpo do Senhor Jesus. <sup><font color=\"#AA66CC\" size=\"-1\">4</font></sup>Estando elas perplexas com o caso, apareceram-lhes dois homens em trajes resplandecentes. <sup><font color=\"#AA66CC\" size=\"-1\">5</font></sup>Como estivessem amedrontadas e voltassem o rosto para o chão, eles disseram-lhes: «Porque buscais o Vivente entre os mortos? <sup><font color=\"#AA66CC\" size=\"-1\">6</font></sup>Não está aqui; ressuscitou! Lembrai-vos de como vos falou, quando ainda estava na Galileia, <br></body></html>";
 	private static final String GLORIOSO2 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>2º Mistério Glorioso</h1><h2>A ascensão de jesus ao céu <sub><font size=\"-1\"><i>(Lc 24,50-51)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">50</font></sup>Depois, levou-os até junto de Betânia e, erguendo as mãos, abençoou-os.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">51</font></sup>Enquanto os abençoava, separou-se deles e elevava-se ao Céu. <br></body></html>";
 	private static final String GLORIOSO3 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>3º Mistério Glorioso</h1><h2>A descida do Espírito Santo sobre Nossa Senhora e os Apóstolos <sub><font size=\"-1\"><i>(At 2,1-4)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">1</font></sup>Quando chegou o dia do Pentecostes, encontravam-se todos reunidos no mesmo lugar. <sup><font color=\"#AA66CC\" size=\"-1\">2</font></sup>De repente, ressoou, vindo do céu, um som comparável ao de forte rajada de vento, que encheu toda a casa onde eles se encontravam.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">3</font></sup>Viram então aparecer umas línguas, à maneira de fogo, que se iam dividindo, e poisou uma sobre cada um deles. <sup><font color=\"#AA66CC\" size=\"-1\">4</font></sup>Todos ficaram cheios do Espírito Santo e começaram a falar outras línguas, conforme o Espírito lhes inspirava que se exprimissem. <br></body></html>";
 	private static final String GLORIOSO4 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>4º Mistério Glorioso</h1><h2>A assunção de Nossa Senhora <sub><font  size=\"-1\"><i>(Ap 21,1-2)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">1</font></sup>Vi, então, um novo céu e uma nova terra, pois o primeiro céu e a primeira terra tinham desaparecido e o mar já não existia.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">2</font></sup>E vi descer do céu, de junto de Deus, a cidade santa, a nova Jerusalém, já preparada, qual noiva adornada para o seu esposo.<br></body></html>";
 	private static final String GLORIOSO5 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>5º mistério Glorioso</h1><h2>A coroação de Nossa Senhora rainha dos Anjos e Santos <sub><font  size=\"-1\"><i>(Ap 12,1)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">1</font></sup>Depois, apareceu no céu um grande sinal: uma Mulher vestida de Sol, com a Lua debaixo dos pés e com uma coroa de doze estrelas na cabeça. <br></body></html>";
 	private static final String GOZOSO1 = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>1º Mistério Gozoso</h1><h2>Anúncio do nascimento de Jesus <sub><font size=\"-1\"><i>(Lc 1,26-38)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">26</font></sup>Ao sexto mês, o anjo Gabriel foi enviado por Deus a uma cidade da Galileia chamada Nazaré, <sup><font color=\"#AA66CC\" size=\"-1\">27</font></sup>a uma virgem desposada com um homem chamado José, da casa de David; e o nome da virgem era Maria.<br /><sup><font color=\"#AA66CC\" size=\"-1\">28</font></sup>Ao entrar em casa dela, o anjo disse-lhe: «Salve, ó cheia de graça, o Senhor está contigo.» <sup><font color=\"#AA66CC\" size=\"-1\">29</font></sup>Ao ouvir estas palavras, ela perturbou-se e inquiria de si própria o que significava tal saudação. <sup><font color=\"#AA66CC\" size=\"-1\">30</font></sup>Disse-lhe o anjo: «Maria, não temas, pois achaste graça diante de Deus. <sup><font color=\"#AA66CC\" size=\"-1\">31</font></sup>Hás-de conceber no teu seio e dar à luz um filho, ao qual porás o nome de Jesus. <sup><font color=\"#AA66CC\" size=\"-1\">32</font></sup>Será grande e vai chamar-se Filho do Altíssimo. O Senhor Deus vai dar-lhe o trono de seu pai David, <sup><font color=\"#AA66CC\" size=\"-1\">33</font></sup>reinará eternamente sobre a casa de Jacob e o seu reinado não terá fim.»<br /><sup><font color=\"#AA66CC\" size=\"-1\">34</font></sup>Maria disse ao anjo: «Como será isso, se eu não conheço homem?» <sup><font color=\"#AA66CC\" size=\"-1\">35</font></sup>O anjo respondeu-lhe: «O Espírito Santo virá sobre ti e a força do Altíssimo estenderá sobre ti a sua sombra. Por isso, aquele que vai nascer é Santo e será chamado Filho de Deus. <sup><font color=\"#AA66CC\" size=\"-1\">36</font></sup>Também a tua parente Isabel concebeu um filho na sua velhice e já está no sexto mês, ela, a quem chamavam estéril, <sup><font color=\"#AA66CC\" size=\"-1\">37</font></sup>porque nada é impossível a Deus.» <sup><font color=\"#AA66CC\" size=\"-1\">38</font></sup>Maria disse, então: «Eis a serva do Senhor, faça-se em mim segundo a tua palavra.» E o anjo retirou-se de junto dela. </body></html>";
 	private static final String GOZOSO2 = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>2º Mistério Gozoso</h1><h2>A visita de Nossa Senhora a Sua prima Santa Isabel <sub><font size=\"-1\"><i>(Lc 1, 39-45)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">39</font></sup>Por aqueles dias, Maria pôs-se a caminho e dirigiu-se à pressa para a montanha, a uma cidade da Judeia. <sup><font color=\"#AA66CC\" size=\"-1\">40</font></sup>Entrou em casa de Zacarias e saudou Isabel. <sup><font color=\"#AA66CC\" size=\"-1\">41</font></sup>Quando Isabel ouviu a saudação de Maria, o menino saltou-lhe de alegria no seio e Isabel ficou cheia do Espírito Santo. <sup><font color=\"#AA66CC\" size=\"-1\">42</font></sup>Então, erguendo a voz, exclamou: «Bendita és tu entre as mulheres e bendito é o fruto do teu ventre. <sup><font color=\"#AA66CC\" size=\"-1\">43</font></sup>E donde me é dado que venha ter comigo a mãe do meu Senhor? <sup><font color=\"#AA66CC\" size=\"-1\">44</font></sup>Pois, logo que chegou aos meus ouvidos a tua saudação, o menino saltou de alegria no meu seio. <sup><font color=\"#AA66CC\" size=\"-1\">45</font></sup>Feliz de ti que acreditaste, porque se vai cumprir tudo o que te foi dito da parte do Senhor.» </body></html>";
 	private static final String GOZOSO3 = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>3º Mistério Gozoso</h1><h2>Nascimento de Jesus em Belém <sub><font size=\"-1\"><i>(Lc 2,4-7)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">4</font></sup>Também José, deixando a cidade de Nazaré, na Galileia, subiu até à Judeia, à cidade de David, chamada Belém, por ser da casa e linhagem de David, <sup><font color=\"#AA66CC\" size=\"-1\">5</font></sup>a fim de se recensear com Maria, sua esposa, que se encontrava grávida.<br /><sup><font color=\"#AA66CC\" size=\"-1\">6</font></sup>E, quando eles ali se encontravam, completaram-se os dias de ela dar à luz <sup><font color=\"#AA66CC\" size=\"-1\">7</font></sup>e teve o seu filho primogénito, que envolveu em panos e recostou numa manjedoura, por não haver lugar para eles na hospedaria.<br /> </body></html>";
 	private static final String GOZOSO4 = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>4º Mistério Gozoso</h1><h2>Apresentação de Jesus no Templo <sub><font size=\"-1\"><i>(Lc 2,22-24)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">22</font></sup>Quando se cumpriu o tempo da sua purificação, segundo a Lei de Moisés, levaram-no a Jerusalém para o apresentarem ao Senhor, <sup><font color=\"#AA66CC\" size=\"-1\">23</font></sup>conforme está escrito na Lei do Senhor: «Todo o primogénito varão será consagrado ao Senhor» <sup><font color=\"#AA66CC\" size=\"-1\">24</font></sup>e para oferecerem em sacrifício, como se diz na Lei do Senhor, duas rolas ou duas pombas. </body></html>";
 	private static final String GOZOSO5 = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div align=\"center\"><h1>5º Mistério Gozoso</h1><h2>Jesus no Templo, entre os Doutores <sub><font size=\"-1\"><i>(Lc 2,41-51)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">41</font></sup>Os pais de Jesus iam todos os anos a Jerusalém, pela festa da Páscoa. <sup><font color=\"#AA66CC\" size=\"-1\">42</font></sup>Quando Ele chegou aos doze anos, subiram até lá, segundo o costume da festa. <sup><font color=\"#AA66CC\" size=\"-1\">43</font></sup>Terminados esses dias, regressaram a casa e o menino ficou em Jerusalém, sem que os pais o soubessem. <sup><font color=\"#AA66CC\" size=\"-1\">44</font></sup>Pensando que Ele se encontrava na caravana, fizeram um dia de viagem e começaram a procurá-lo entre os parentes e conhecidos. <sup><font color=\"#AA66CC\" size=\"-1\">45</font></sup>Não o tendo encontrado, voltaram a Jerusalém, à sua procura.<br /><sup><font color=\"#AA66CC\" size=\"-1\">46</font></sup>Três dias depois, encontraram-no no templo, sentado entre os doutores, a ouvi-los e a fazer-lhes perguntas. <sup><font color=\"#AA66CC\" size=\"-1\">47</font></sup>Todos quantos o ouviam, estavam estupefactos com a sua inteligência e as suas respostas.<br /><sup><font color=\"#AA66CC\" size=\"-1\">48</font></sup>Ao vê-lo, ficaram assombrados e sua mãe disse-lhe: «Filho, porque nos fizeste isto? Olha que teu pai e eu andávamos aflitos à tua procura!» <sup><font color=\"#AA66CC\" size=\"-1\">49</font></sup>Ele respondeu-lhes: «Porque me procuráveis? Não sabíeis que devia estar em casa de meu Pai?»<br /><sup><font color=\"#AA66CC\" size=\"-1\">50</font></sup>Mas eles não compreenderam as palavras que lhes disse.<br /><sup><font color=\"#AA66CC\" size=\"-1\">51</font></sup>Depois desceu com eles, voltou para Nazaré e era-lhes submisso. Sua mãe guardava todas estas coisas no seu coração.</body></html>";
 	private static final String ORACOESFINAIS = "<html><head></head><body>Louvado seja Nosso Senhor Jesus Cristo<br />Para sempre seja louvado<br />com Sua mãe Maria Santíssima.</body></html>";
 	private static final String JACULATORIAS = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Jaculatórias</title><meta name=\"generator\" content=\"Bluefish 2.2.2\" ><meta name=\"author\" content=\"Alexandre Carlos\" ><meta name=\"date\" content=\"2012-10-08T12:34:01+0100\" ><meta name=\"copyright\" content=\"\"><meta name=\"keywords\" content=\"\"><meta name=\"description\" content=\"\"><meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\"><meta http-equiv=\"content-style-type\" content=\"text/css\"><meta http-equiv=\"expires\" content=\"0\"></head><body><h1 style=\"text-align: center; color: #0000FF;\">Jaculatórias</h1>Ó Maria concebida sem pecado<br />Rogai por nós que recorremos a vós.<br />Ó meu Jesus, perdoai-nos e livrai-nos do fogo do inferno <br />Levai as almas todas para o Céu, principalmente as que mais precisarem.</body></html>";
 	private static final String LUMINOSO1 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div  align=\"center\"><h1>1º Mistério Luminoso</h1><h2>O baptismo de Jesus no Jordão <sub><font  size=\"-1\"><i>(Mt 3,13-17)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">13</font></sup>Então, veio Jesus da Galileia ao Jordão ter com João, para ser baptizado por ele. <sup><font color=\"#AA66CC\" size=\"-1\">14</font></sup>João opunha-se, dizendo: «Eu é que tenho necessidade de ser baptizado por ti, e Tu vens a mim?» <sup><font color=\"#AA66CC\" size=\"-1\">15</font></sup>Jesus, porém, respondeu-lhe: «Deixa por agora. Convém que cumpramos assim toda a justiça.» João, então, concordou.<br><br><sup><font color=\"#AA66CC\" size=\"-1\">16</font></sup>Uma vez baptizado, Jesus saiu da água e eis que se rasgaram os céus, e viu o Espírito de Deus descer como uma pomba e vir sobre Ele. <sup><font color=\"#AA66CC\" size=\"-1\">17</font></sup>E uma voz vinda do Céu dizia: «Este é o meu Filho muito amado, no qual pus todo o meu agrado.»<br><br><br></body></html>";
 	private static final String LUMINOSO2 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div  align=\"center\"><h1>2º Mistério Luminoso</h1><h2>A revelação de Jesus nas bodas de Caná <sub><font size=\"-1\"><i>(Jo 2,1-5.11)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">1</font></sup>Ao terceiro dia, celebrava-se uma boda em Caná da Galileia e a mãe de Jesus estava lá. <sup><font color=\"#AA66CC\" size=\"-1\">2</font></sup>Jesus e os seus discípulos também foram convidados para a boda. <sup><font color=\"#AA66CC\" size=\"-1\">3</font></sup>Como viesse a faltar o vinho, a mãe de Jesus disse-lhe: «Não têm vinho!»<br><br><sup><font color=\"#AA66CC\" size=\"-1\">4</font></sup>Jesus respondeu-lhe: «Mulher, que tem isso a ver contigo e comigo? Ainda não chegou a minha hora.» <sup><font color=\"#AA66CC\" size=\"-1\">5</font></sup>Sua mãe disse aos serventes: «Fazei o que Ele vos disser!» <br><br><sup><font color=\"#AA66CC\" size=\"-1\">11</font></sup>Assim, em Caná da Galileia, Jesus realizou o primeiro dos seus sinais miraculosos, com o qual manifestou a sua glória, e os discípulos creram nele. <br></body></html>";
 	private static final String LUMINOSO3 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div  align=\"center\"><h1>3º Mistério Luminoso</h1><h2>O anúncio do Reino de Deus com o convite à conversão <sub><font  size=\"-1\"><i>(Mc 1,14-15)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">14</font></sup>Depois de João ter sido preso, Jesus foi para a Galileia, e proclamava o Evangelho de Deus, <sup><font color=\"#AA66CC\" size=\"-1\">15</font></sup>dizendo: «Completou-se o tempo e o Reino de Deus está próximo: arrependei-vos e acreditai no Evangelho.» <br></body></html>";
 	private static final String LUMINOSO4 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div  align=\"center\"><h1>4º Mistério Luminoso</h1><h2>A transfiguração do Senhor <sub><font  size=\"-1\"><i>(Mt 17,1-8)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">1</font></sup>Seis dias depois, Jesus tomou consigo Pedro, Tiago e seu irmão João, e levou-os, só a eles, a um alto monte. <sup><font color=\"#AA66CC\" size=\"-1\">2</font></sup>Transfigurou-se diante deles: o seu rosto resplandeceu como o Sol, e as suas vestes tornaram-se brancas como a luz. <sup><font color=\"#AA66CC\" size=\"-1\">3</font></sup>Nisto, apareceram Moisés e Elias a conversar com Ele. <sup><font color=\"#AA66CC\" size=\"-1\">4</font></sup>Tomando a palavra, Pedro disse a Jesus: «Senhor, é bom estarmos aqui; se quiseres, farei aqui três tendas: uma para ti, uma para Moisés e outra para Elias.» <sup><font color=\"#AA66CC\" size=\"-1\">5</font></sup>Ainda ele estava a falar, quando uma nuvem luminosa os cobriu com a sua sombra, e uma voz dizia da nuvem: «Este é o meu Filho muito amado, no qual pus todo o meu agrado. Escutai-o.»<br><br><sup><font color=\"#AA66CC\" size=\"-1\">6</font></sup>Ao ouvirem isto, os discípulos caíram com a face por terra, muito assustados. <sup><font color=\"#AA66CC\" size=\"-1\">7</font></sup>Aproximando-se deles, Jesus tocou-lhes, dizendo: «Levantai-vos e não tenhais medo.» <sup><font color=\"#AA66CC\" size=\"-1\">8</font></sup>Erguendo os olhos, os discípulos apenas viram Jesus e mais ninguém. <br></body></html>";
 	private static final String LUMINOSO5 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body><div  align=\"center\"><h1>5º Mistério Luminoso</h1><h2>A instituição da Eucaristia <sub><font  size=\"-1\"><i>(Mt 26,26-29)</i></font></sub></h2></div><sup><font color=\"#AA66CC\" size=\"-1\">26</font></sup>Enquanto comiam, Jesus tomou o pão e, depois de pronunciar a bênção, partiu-o e deu-o aos seus discípulos, dizendo: «Tomai, comei: Isto é o meu corpo.»<br><br><sup><font color=\"#AA66CC\" size=\"-1\">27</font></sup>Em seguida, tomou um cálice, deu graças e entregou-lho, dizendo: «Bebei dele todos. <sup><font color=\"#AA66CC\" size=\"-1\">28</font></sup>Porque este é o meu sangue, sangue da Aliança, que vai ser derramado por muitos, para perdão dos pecados.<sup><font color=\"#AA66CC\" size=\"-1\">29</font></sup>Eu vos digo: Não beberei mais deste produto da videira, até ao dia em que beber o vinho novo convosco no Reino de meu Pai.»  <br></body></html>";
 	private static final String PAINOSSO = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Pai Nosso</title></head><body><h1 style=\"text-align: center; color: #0000FF;\">Pai Nosso</h1>Pai Nosso que estais no céu,<br />santificado seja o vosso nome,<br />vem a nós o vosso reino,<br />seja feita a vossa vontade<br />assim na terra como no céu.<br /><br />O pão nosso de cada dia nos daí hoje,<br />perdoai-nos as nossas ofensas,<br />assim como nós perdoamos<br />a quem nos tem ofendido,<br />não nos deixei cair em tentação<br />mas livrai-nos do mal.<br /><br />Amen<br /><hr size=\"2\" /></body></html>";
 	private static final String SALVERAINHA = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Salve Rainha</title><meta name=\"generator\" content=\"Bluefish 2.2.2\" ><meta name=\"author\" content=\"Alexandre Carlos\" ><meta name=\"date\" content=\"2012-10-08T12:34:01+0100\" ><meta name=\"copyright\" content=\"\"><meta name=\"keywords\" content=\"\"><meta name=\"description\" content=\"\"><meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\"><meta http-equiv=\"content-style-type\" content=\"text/css\"><meta http-equiv=\"expires\" content=\"0\"><meta ><meta ></head><body><h1 style=\"text-align: center; color: #0000FF;\">Salve Rainha</h1>Salve Rainha,<br />Mãe de Misericórdia,<br />vida e doçura esperança nossa salve!<br />A vós bradamos degredados filho de Eva.<br />A vós suspiramos gemendo e chorando neste<br />vale de lágrimas.<br />Eia pois advogada nossa<br />esses vossos olhos misericordiosos a nós volvei,<br />e depois deste desterro mostrai Jesus bendito fruto em vosso ventre,<br /><br />ó clemente,<br />ó piedosa<br />ó doce e Santa Virgem Maria.<br /><br />Rogai por nós Santa mãe de Deus.<br />Para que sejamos sempre livre do pecado,<br />protegido de todos os perigos<br />e dignos da promessa de Cristo.<br /><br />Amen<br /></body></html>";
 
 	/**
 	 * @param dia_semana
 	 *            int com o dia da semana 1 - Domingo 2 - 2ª Feira 3 - 3ª Feira
 	 *            4 - 4ª Feira 5 - 5ª Feira 6 - 6ª Feira 7 - Sábado
 	 * @return String Array com os Mistérios associados ao dia da semana
 	 */
 	public static String[] Design__Misterios(int dia_semana) {
 		switch (MISTERIOS[dia_semana]) {
 		case 0:
 			return GLORIOSOS;
 		case 1:
 			return GOZOSOS;
 		case 2:
 			return DOLOROSOS;
 		case 3:
 			return LUMINOSOS;
 		default:
 			break;
 		}
 
 		return null;
 	}
 
 	public static String Obter_Misterio_do_Dia(int dia_semana,
 			int misterio) {
 		
 		switch (MISTERIOS[dia_semana]) {
 		case 0:
 			return Gloriosos(misterio);
 		case 1:
 			return Gozosos(misterio);
 		case 2:
 			return Dolorosos(misterio);
 		case 3:
 			return Luminosos(misterio);
 		}
 
 		return null;
 
 	}
 
 	public static final String Gozosos(int i) {
 
 		switch (i) {
 		case 0:
 			return GOZOSO1;
 		case 1:
 			return GOZOSO2;
 		case 2:
 			return GOZOSO3;
 		case 3:
 			return GOZOSO4;
 		case 4:
 			return GOZOSO5;
 		case 5:
 			return ORACOESFINAIS;
 		}
 
 		return null;
 	}
 
 	public static final String Gloriosos(int i) {
 
 		switch (i) {
 		case 0:
 			return GLORIOSO1;
 		case 1:
 			return GLORIOSO2;
 		case 2:
 			return GLORIOSO3;
 		case 3:
 			return GLORIOSO4;
 		case 4:
 			return GLORIOSO5;
 		case 5:
 			return ORACOESFINAIS;
 		}
 
 		return null;
 	}
 
 	public static final String Dolorosos(int i) {
 
 		switch (i) {
 		case 0:
 			return DOLOROSO1;
 		case 1:
 			return DOLOROSO2;
 		case 2:
 			return DOLOROSO3;
 		case 3:
 			return DOLOROSO4;
 		case 4:
 			return DOLOROSO5;
 		case 5:
 			return ORACOESFINAIS;
 		}
 
 		return null;
 	}
 
 	public static final String Luminosos(int i) {
 
 		switch (i) {
 		case 0:
 			return LUMINOSO1;
 		case 1:
 			return LUMINOSO2;
 		case 2:
 			return LUMINOSO3;
 		case 3:
 			return LUMINOSO4;
 		case 4:
 			return LUMINOSO5;
 		case 5:
 			return ORACOESFINAIS;
 		}
 
 		return null;
 	}
 
 	public static final String PaiNosso() {
 		return PAINOSSO;
 	}
 
 	public static final String AveMaria() {
 		return AVE_MARIA;
 	}
 
 	public static String Gloria() {
 		return GLORIA;
 	}
 
 	public static String SalveRainha() {
 		return SALVERAINHA;
 	}
 
 	public static String Jaculatoria() {
 		return JACULATORIAS;
 	}
 
 	public static List <String> Oracoes_do_Misterio(int dia_semana, int misterio) {
 		List <String> oracao = new ArrayList<String>();
 		
 		try {
 			oracao.add(Obter_Misterio_do_Dia(dia_semana, misterio));
			oracao.add(PaiNosso());
			for (int i = 2; i < 12; i++) {
				oracao.add(AveMaria());
 			}
			oracao.add(Gloria());
			oracao.add(Jaculatoria());
 			return oracao;
 		} catch (Exception e) {
 			Log.e(TAG, "Exception: dia_semana="+dia_semana+"; misterio="+misterio);
 			Log.e(TAG, "Oracoes_do_Misterio", e);
 		}
 		
 		return oracao;
 	}
 	
 	
 }
