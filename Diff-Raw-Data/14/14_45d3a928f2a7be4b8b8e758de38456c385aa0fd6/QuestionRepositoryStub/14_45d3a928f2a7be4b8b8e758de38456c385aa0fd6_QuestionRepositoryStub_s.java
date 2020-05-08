 package be.noselus.repository;
 
 import be.noselus.model.Person;
 import be.noselus.model.Question;
 import com.google.common.collect.Lists;
 import org.joda.time.LocalDate;
 
 import java.util.List;
 
 public class QuestionRepositoryStub implements QuestionRepository {
     @Override
     public List<Question> getQuestions() {
 
         Person askingPerson = new Person("DISABATO", "Emmanuel");
         Person askedToPerson = new Person("FURLAN", "Paul");
 
         Question question = new Question(askingPerson,askedToPerson,askedToPerson,"2010-2011",2011, "594 (2010-2011) 1", LocalDate.parse("2011-08-29"), LocalDate.parse("2011-10-07"),
                 "Open Data - Open Government",
                 "Les informations publiques, ou données ouvertes (open data), sont aujourd'hui en termes de potentiel, d'opportunités et d'enjeu au c?ur du développement de multiples innovations sociales et économiques. En effet, grâce à leur mise en ligne par l'administration et les collectivités territoriales, elles peuvent contribuer à la création de nouveaux services tout en favorisant la transparence de l'action publique.\n" +
                         "\n" +
                         "Dans ce cadre, des pays comme la France, les Etats-Unis, la Norvège ou encore l'Angleterre, utilisent cette nouvelle forme de gouvernance. En effet, tout organisme d'une certaine taille collecte, génère ou maintient un important volume de données électroniques (bases de données, systèmes d'information cartographiques, registres électroniques, etc.).\n" +
                         "\n" +
                         "Ainsi, à titre d'exemple, les données que possède la Ville de Paris sont exploitées au mieux par les services municipaux dans le cadre de leurs missions, mais constituent également un patrimoine immatériel qui peut être mis en valeur pour l'ensemble de la collectivité :\n" +
                         "- les chercheurs peuvent y trouver matière à nourrir leurs travaux et expériences ;\n" +
                         "- les développeurs peuvent créer des services innovants utilisant ces données ;\n" +
                         "- les citoyens et journalistes y trouvent des informations brutes ;\n" +
                         "- les entreprises peuvent fournir une valeur ajoutée à ces données, et ainsi créer de l'emploi et de la richesse pour la collectivité.\n" +
                         "\n" +
                         "En janvier 2011, la Ville de Paris, à l'initiative de son maire et de son adjoint en charge de l'innovation, de la recherche et des universités, a permis l'ouverture de ce site, marquant la fin d'un premier cycle d'exploration des données de la ville, mais surtout le début d'une nouvelle ère en matière de transparence. Cette démarche est intéressante et innovante, car elle permet aux habitants de devenir des co-concepteurs des évolutions de leur ville.\n" +
                         "\n" +
                         "Cette démarche se fait, bien évidemment, dans le respect des règles sur la vie privée et il est clair qu'il n'est pas question de donner des informations sur des données personnelles, mais bien des informations d'ordre général.\n" +
                         "\n" +
                         "Monsieur le Ministre a-t-il déjà entendu parler de ce concept ?\n" +
                         "Pense-t-il qu'il serait envisageable de le mettre en place dans notre région ?\n" +
                         "Concernant les communes wallonnes, ne serait-il pas intéressant d'étudier la question et, pourquoi pas, développer une expérience pilote en la matière ?\n" +
                         "Monsieur le Ministre a-t-il déjà été approché par des sociétés informatiques sur ce sujet ?",
                 "Il n'est pas inutile de rappeler l'origine de la matière. Le Principe d'Open Data, c'est-à-dire la réutilisation des informations du secteur public, trouve sa source dans la Directive 2003/98 du même nom et à pour objet d'établir un cadre harmonisé fixant les conditions de réutilisation desdites informations.\n" +
                         "\n" +
                         "Par « réutilisation », on vise l'utilisation d'informations détenues par des organismes du secteur public à des fins commerciales ou non commerciales autres que l'objectif initial de la mission de service public pour lequel les informations ont été produites.\n" +
                         "\n" +
                         "Ainsi, dans le cadre de l'exercice de ses multiples missions, le SPW collecte ou produit une grande variété d'informations afin :\n" +
                         "- de préparer ses décisions,\n" +
                         "- de fournir ses services,\n" +
                         "- d'évaluer son action.\n" +
                         "\n" +
                         "Appliqué au secteur public, le principe de l'Open Data consiste donc à permettre l'usage, commercial ou non, par d'autres acteurs (organismes publics, entreprises, associations, pouvoirs locaux ou citoyens) des données collectées par un organisme public donné.\n" +
                         "C'est en 2006 que le gouvernement adoptait définitivement le décret transposant la directive précitée.\n");
 
        return Lists.newArrayList(question);
     }
 }
