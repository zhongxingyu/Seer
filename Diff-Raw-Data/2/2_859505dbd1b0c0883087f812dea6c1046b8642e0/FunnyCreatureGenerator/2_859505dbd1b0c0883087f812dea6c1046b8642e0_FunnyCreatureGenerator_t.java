 package com.ilsoft.funnycreatures.core;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.util.Log;
 
 import com.ilsoft.funnycreatures.core.RootBase.RootLink;
 
 public class FunnyCreatureGenerator
 {
 	public static final int ROOT_FALSE = 601;
 	
 	public static final RootBase[] roots = new RootBase[]
 	{
 		// Животнов(о)
 		new RootBase(0, "кошач", RootLink.E),
 		new RootBase(1, "утк", RootLink.O),
 		new RootBase(2, "зайц", RootLink.E),
 		new RootBase(3, "тигр", RootLink.O),
 		new RootBase(4, "крыс", RootLink.O),
 		new RootBase(5, "медвед", RootLink.E),
 		new RootBase(6, "дятл", RootLink.O),
 		new RootBase(7, "тюлен", RootLink.E),
 		new RootBase(8, "слон", RootLink.O),
 		new RootBase(9, "ёжиковидн", RootLink.O),
 
 		// Частетельно
 		new RootBase(100, "хвост", RootLink.O),
 		new RootBase(101, "ух", RootLink.O),
 		new RootBase(102, "лап", RootLink.O),
 		new RootBase(103, "лапчат", RootLink.O),
 		new RootBase(104, "морд", RootLink.O),
 		new RootBase(105, "зад", RootLink.O),
 		new RootBase(106, "нос", RootLink.O),
 		new RootBase(107, "крыл", RootLink.O),
 		new RootBase(108, "пуз", RootLink.O),
 		new RootBase(109, "клюв", RootLink.O),
 		new RootBase(110, "хобот", RootLink.O),
 		new RootBase(111, "ноздр", RootLink.E),
 		new RootBase(112, "клыкаст", RootLink.O),
 
 		// Количественно
 		new RootBase(200, "без", RootLink.EMPTY),
 		new RootBase(201, "един", RootLink.O),
 		new RootBase(202, "дву", RootLink.EMPTY),
 		new RootBase(203, "трёх", RootLink.EMPTY),
 		new RootBase(204, "четырех", RootLink.EMPTY),
 		new RootBase(205, "пяти", RootLink.EMPTY),
 		
 		// Внешне
 		new RootBase(300, "грабл", RootLink.E),
 		new RootBase(301, "кос", RootLink.O),
 		new RootBase(302, "шишк", RootLink.O),
 		new RootBase(303, "рог", RootLink.O),
 		new RootBase(304, "перепончат", RootLink.O),
 		new RootBase(305, "гладк", RootLink.O),
 		new RootBase(306, "полосат", RootLink.O),
 		new RootBase(307, "пятнист", RootLink.O),
 		new RootBase(308, "туп", RootLink.O),
 		new RootBase(309, "остр", RootLink.O),
 		new RootBase(310, "длинн", RootLink.O),
 		new RootBase(311, "коротк", RootLink.O),
 		new RootBase(312, "выпукл", RootLink.O),
 		new RootBase(313, "узк", RootLink.O),
 		new RootBase(314, "продолговат", RootLink.O),
 		new RootBase(315, "висл", RootLink.O),
 		new RootBase(316, "торчк", RootLink.O),
 		new RootBase(317, "шипаст", RootLink.O),
 		new RootBase(318, "толст", RootLink.O),
 		new RootBase(319, "бледн", RootLink.O),
 
 		new RootBase(409, "шерст", RootLink.E),
 		new RootBase(411, "чешуйчат", RootLink.O),
 
 		new RootBase(500, "растопыр", RootLink.O),
 		new RootBase(501, "гребубл", RootLink.E),
 		new RootBase(502, "лупогляд", RootLink.O),
 		new RootBase(503, "кусачк", RootLink.O),
 		new RootBase(504, "царапк", RootLink.O),
 		new RootBase(505, "выполз", RootLink.O),
 		new RootBase(506, "увал", RootLink.O),
 		new RootBase(507, "невид", RootLink.O),
 		new RootBase(508, "отгрызайк", RootLink.O),
 		new RootBase(509, "уползад", RootLink.O),
 		new RootBase(510, "грибоед", RootLink.O),
 		new RootBase(511, "камнегрыз", RootLink.O),
 		new RootBase(512, "древожу", RootLink.E),
 		new RootBase(513, "тормоз", RootLink.O),
 		new RootBase(514, "шерст", RootLink.E),
 		new RootBase(515, "злобнозырк", RootLink.O),
 		
 		new RootBase(600, "обыкновенн", RootLink.O),
 		new RootBase(ROOT_FALSE, "ложн", RootLink.O),
 		new RootBase(602, "пустынн", RootLink.O),
 		new RootBase(603, "речн", RootLink.O),
 		new RootBase(604, "лесн", RootLink.O),
 		new RootBase(605, "песчан", RootLink.O),
 		new RootBase(606, "закустов", RootLink.O),
 		new RootBase(607, "болотн", RootLink.O),
 		new RootBase(608, "травянист", RootLink.O),
 		new RootBase(609, "подкамнев", RootLink.O),
 	};	
 	
 	public static RootBase getRoot(int id)
 	{
 		for (int i = 0; i < roots.length; i++)
 			if (roots[i].getId() == id) return roots[i];
 		return null;
 	}
 	
 	public static final Adjective[] adjectiveLastParts = new Adjective[]
 	{
 /*		new Adjective(getRoot(0), AdjectiveBase.SOFT),
 		new Adjective(getRoot(1), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(2), Suffix.EV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(3), Suffix.OV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(getRoot(4), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(5), Suffix.EV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(6), Suffix.OV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(7), Suffix.EV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(8), Suffix.OV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(getRoot(9), AdjectiveBase.HARD),*/
 		
 		new Adjective(getRoot(100), AdjectiveBase.HARD),
 		new Adjective(getRoot(101), AdjectiveBase.SOFT),
 		new Adjective(getRoot(102), AdjectiveBase.HARD),
 		new Adjective(getRoot(103), AdjectiveBase.HARD),
 		new Adjective(getRoot(104), AdjectiveBase.HARD),
 		new Adjective(getRoot(105), AdjectiveBase.HARD),
 		new Adjective(getRoot(106), AdjectiveBase.HARD),
 		new Adjective(getRoot(107), AdjectiveBase.HARD),
 		new Adjective(getRoot(108), AdjectiveBase.HARD),
 		new Adjective(getRoot(109), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(110), Suffix.N, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(111), Suffix.YAN, RootLink.O), AdjectiveBase.OLD),
 		new Adjective(getRoot(112), AdjectiveBase.HARD),
 
 		new Adjective(new RootWithSuffixes(getRoot(300), Suffix.YAN, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(getRoot(301), AdjectiveBase.OLD),
 		new Adjective(new RootWithSuffixes(new RootWithSuffixes(getRoot(302), Suffix.OV, RootLink.O), Suffix.AT, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(303), Suffix.OV, RootLink.O), AdjectiveBase.OLD),
 		new Adjective(getRoot(304), AdjectiveBase.HARD),
 		new Adjective(getRoot(305), AdjectiveBase.SOFT),
 		new Adjective(getRoot(306), AdjectiveBase.HARD),
 		new Adjective(getRoot(307), AdjectiveBase.HARD),
 		new Adjective(getRoot(308), AdjectiveBase.OLD),
 		new Adjective(getRoot(309), AdjectiveBase.HARD),
 		new Adjective(getRoot(310), AdjectiveBase.HARD),
 		new Adjective(getRoot(311), AdjectiveBase.SOFT),
 		new Adjective(getRoot(312), AdjectiveBase.HARD),
 		new Adjective(getRoot(313), AdjectiveBase.SOFT),
 		new Adjective(getRoot(314), AdjectiveBase.HARD),
 		new Adjective(getRoot(315), AdjectiveBase.HARD),
 		new Adjective(new RootWithSuffixes(getRoot(316), Suffix.OV, RootLink.O), AdjectiveBase.HARD),
 		new Adjective(getRoot(317), AdjectiveBase.HARD),
 		new Adjective(getRoot(318), AdjectiveBase.HARD),
 		new Adjective(getRoot(319), AdjectiveBase.HARD),
 
 		new Adjective(new RootWithSuffixes(getRoot(409), Suffix.N, RootLink.O), AdjectiveBase.HARD),
 		
 		new Adjective
 		(
 			new RootWithSuffixes
 			(
 				new RootWithSuffixes
 				(
 					getRoot(500), 
 					Suffix.CH, RootLink.E
 				), 
 				Suffix.AT, RootLink.O
 			),
 			AdjectiveBase.HARD
 		),
 		
 		new Adjective(getRoot(600), AdjectiveBase.HARD),
 		new Adjective(getRoot(ROOT_FALSE), AdjectiveBase.HARD),
 		new Adjective(getRoot(602), AdjectiveBase.HARD),
 		new Adjective(getRoot(603), AdjectiveBase.OLD),
 		new Adjective(getRoot(604), AdjectiveBase.OLD),
 		new Adjective(getRoot(605), AdjectiveBase.HARD),
 		new Adjective(getRoot(606), AdjectiveBase.HARD),
 		new Adjective(getRoot(607), AdjectiveBase.HARD),
 		new Adjective(getRoot(608), AdjectiveBase.HARD),
 		new Adjective(getRoot(609), AdjectiveBase.HARD),
 	};
 
 	public static Adjective getAdjectiveLastPart(int id)
 	{
 		for (int i = 0; i < adjectiveLastParts.length; i++)
 		{
 			if (adjectiveLastParts[i].getRoot().getId() == id)
 			{
 				return adjectiveLastParts[i];
 			}
 		}
 		return null;
 	}
 
 	public static final Noun[] nounLastParts = new Noun[]
 	{
 		new Noun(getRoot(100), NounBase.M_HARD_1),
 		new Noun(getRoot(101), NounBase.M_HARD_1),
 		new Noun(getRoot(102), NounBase.M_HARD_1),
 		new Noun(getRoot(104), NounBase.M_HARD_1),
 		new Noun(getRoot(105), NounBase.M_HARD_1),
 		new Noun(getRoot(106), NounBase.M_HARD_1),
 		new Noun(getRoot(107), NounBase.M_HARD_1),
 		new Noun(getRoot(108), NounBase.M_HARD_1),
 		new Noun(getRoot(109), NounBase.M_HARD_1),
 
 		new Noun(getRoot(303), NounBase.F_HARD_1),
 
 		new Noun(new RootWithSuffixes(getRoot(500), Suffix.K, RootLink.O), NounBase.F_HARD_1),
 		new Noun(getRoot(501), NounBase.F_SOFT_1),
 		new Noun(getRoot(502), NounBase.M_2),
 		new Noun(getRoot(503), NounBase.F_HARD_1),
 		new Noun(getRoot(504), NounBase.F_HARD_1),
 		new Noun(getRoot(505), NounBase.M_2),
 		new Noun(getRoot(506), NounBase.M_2),
 		new Noun(getRoot(507), NounBase.M_2),
 		new Noun(getRoot(508), NounBase.F_HARD_1),
 		new Noun(getRoot(509), NounBase.M_HARD_1),
 		new Noun(getRoot(510), NounBase.M_HARD_1),
 		new Noun(getRoot(511), NounBase.M_HARD_1),
 		new Noun(getRoot(512), NounBase.M_SOFT_1),
 		new Noun(getRoot(513), NounBase.M_2),
 		new Noun(getRoot(514), NounBase.M_2),
 		new Noun(getRoot(515), NounBase.M_HARD_1),
 	};
 	
 	public static Noun getNounLastPart(int id)
 	{
 		for (int i = 0; i < nounLastParts.length; i++)
 		{
 			if (nounLastParts[i].getRoot().getId() == id)
 			{
 				return nounLastParts[i];
 			}
 		}
 		return null;
 	}
 	
 	
 	public static final int[] firstRootParts = new int[]
 	{
 		0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
 		100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 
 		200, 201, 202, 203, 204, 205,
 		300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319,
 	};
 
 	public static final int[] lastRootParts = new int[]
 	{
 		100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
 		303, 
 		409,
 	};
 	
 	public static final int[] singleAdjectiveRoots = new int[]
 	{
 		600, ROOT_FALSE, 602, 603, 604, 605, 606, 607, 608, 609,
 	};
 
 	public static final int[] singleNounRoots = new int[]
 	{
		500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515
 	};
 	
 	public static final int[] lastNounRoots = new int[]
 	{
 		100, 101, 102, 104, 105, 106, 107, 108, 109,
 		303,
 	};
 	
 	private static IdPair[] bannedPairs = new IdPair[]
 	{
 		// Единственная часть тела по умолчанию
 		new IdPair(201, 100),
 		new IdPair(201, 104),
 		new IdPair(201, 105),
 		new IdPair(201, 106),
 		new IdPair(201, 108),
 		new IdPair(201, 109),
 		new IdPair(201, 110),
 
 		// Четвероногий - это скучно
 		new IdPair(204, 102),
 
 		// Слишком много про шерсть
 		new IdPair(409, 513),
 		
 		// Тавтологии с лапами
 		new IdPair(102, 103),	// лаполапчатый 
 		new IdPair(103, 102),	// лапчатолапый
 	
 	};
 	
 	private static IdPair[] bannedGlobals = new IdPair[]
 	{
 		new IdPair(609, 511)	// подкамневый камнегрыз
 	};
 	
 	private static boolean isBanned(Integer[] ids)
 	{
 		for (int i = 0; i < ids.length - 1; i++)
 		for (int j = i + 1; j < ids.length; j++)
 		{
 			// Equal IDs are not allowed
 			if (ids[i].equals(ids[j])) return true;
 			
 			// Checking if the global is banned
 			for (int k = 0; k < bannedGlobals.length; k++)
 			{
 				if (bannedGlobals[k].getId1() == ids[i] && bannedGlobals[k].getId2() == ids[j])
 				{
 					return true;
 				}
 			}
 		}
 		
 		// Checking for banned pairs
 		for (int i = 0; i < ids.length - 1; i++)
 		{
 			// Checking if the pair is banned
 			for (int k = 0; k < bannedPairs.length; k++)
 			{
 				if (bannedPairs[k].getId1() == ids[i] && bannedPairs[k].getId2() == ids[i + 1])
 				{
 					return true;
 				}
 			}
 			
 		}
 		
 		return false;
 	}
 	
 	public static FunnyCreature generate(Random rnd)
 	{
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 
 		int adjectivesNumber = rnd.nextInt(2) + 1;
 		
 		boolean[] adjectiveIsCompound = new boolean[adjectivesNumber];
 		for (int i = 0; i < adjectivesNumber; i++)
 		{
 			adjectiveIsCompound[i] = rnd.nextBoolean();
 		}
 		
 		boolean nounIsCompound = rnd.nextBoolean();
 		
 		do
 		{
 			ids.clear();
 			
 			// Creating the adjectives
 			for (int i = 0; i < adjectivesNumber; i++)
 			{
 				if (adjectiveIsCompound[i])
 				{
 					// Generating the last adjective root
 					ids.add(lastRootParts[rnd.nextInt(lastRootParts.length)]);
 
 					// Generating first adjective root
 					ids.add(firstRootParts[rnd.nextInt(firstRootParts.length)]);
 				}
 				else
 				{
 					ids.add(singleAdjectiveRoots[rnd.nextInt(singleAdjectiveRoots.length)]);
 				}
 			}
 			
 			// Creating the noun
 			if (nounIsCompound)
 			{
 				ids.add(lastNounRoots[rnd.nextInt(lastNounRoots.length)]);
 				ids.add(firstRootParts[rnd.nextInt(firstRootParts.length)]);
 			}
 			else
 			{
 				ids.add(singleNounRoots[rnd.nextInt(singleNounRoots.length)]);
 			}
 		}
 		while (isBanned(ids.toArray(new Integer[] {})));
 		
 		Log.d("FunnyCreatureGenerator", ids.toString());
 		
 		// Generating
 		Adjective[] adjectives = new Adjective[adjectivesNumber];
 		int idIndex = 0;
 		for (int i = 0; i < adjectivesNumber; i++)
 		{
 			if (adjectiveIsCompound[i])
 			{
 				adjectives[i] = getAdjectiveLastPart(ids.get(idIndex));
 				idIndex ++;
 				adjectives[i] = new Adjective(getRoot(ids.get(idIndex)), adjectives[i]);
 			}
 			else
 			{
 				adjectives[i] = getAdjectiveLastPart(ids.get(idIndex));
 			}
 			idIndex ++;
 		}
 		
 		Noun noun;
 		if (nounIsCompound)
 		{
 			noun = getNounLastPart(ids.get(idIndex));
 			idIndex ++;
 			noun = new Noun(getRoot(ids.get(idIndex)), noun);
 		}
 		else
 		{
 			noun = getNounLastPart(ids.get(idIndex));
 		}
 
 		FunnyCreature generatedCreature = new FunnyCreature(adjectives, noun);
 		
 		return generatedCreature;
 	}
 
 }
