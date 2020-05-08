 package pl.booone.iplay.utilities.hibernate.processors;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import pl.booone.iplay.models.ageratings.pegi.PegiEnum;
 import pl.booone.iplay.models.companies.CompanyDAO;
 import pl.booone.iplay.models.companies.CompanyDTO;
 import pl.booone.iplay.models.games.GameDTO;
 import pl.booone.iplay.models.games.GameStatusEnum;
 import pl.booone.iplay.models.gametypes.GameTypeDAO;
 import pl.booone.iplay.models.gametypes.GameTypeDTO;
 import pl.booone.iplay.models.gametypes.GameTypeEnum;
 import pl.booone.iplay.models.platforms.PlatformDAO;
 import pl.booone.iplay.models.platforms.PlatformDTO;
 import pl.booone.iplay.models.platforms.PlatformEnum;
 import pl.booone.iplay.utilities.hibernate.processors.exceptions.TestGameProcessorException;
 import pl.booone.iplay.utilities.loggers.GeneralLogger;
 import pl.booone.iplay.utilities.string.DateConverter;
 
 import java.text.ParseException;
 import java.util.*;
 
 import static pl.booone.iplay.utilities.hibernate.processors.TestGameProcessor.FieldType.*;
 
 /**
  * Creates a GameDTO from format:
  * |Title|dd.MM.yyyy premiereDate|PEGI18|FPP,SHOOTER
  * |PlatformDTO,PlatformDTO|CompanyDTO,CompanyDTO|CompanyDTO,CompanyDTO|points|Status|dd.MM.yyy HH:mm:ss addedDate
  */
 @Component
 public class TestGameProcessor
 {
     private static final String FIXED_GAME_FIELD_SEPARATOR = "|";
     private static final String FIXED_GAME_ARRAY_SEPARATOR = ",";
 
     @Autowired
     private CompanyDAO companyDAO;
     @Autowired
     private GameTypeDAO gameTypeDAO;
     @Autowired
     private PlatformDAO platformDAO;
 
     public GameDTO getGameDTOFromFixedString(String fixedGame) throws TestGameProcessorException
     {
         if (fixedGame == null)
         {
             throw new TestGameProcessorException("Trying to create UserDTO from null String");
         }
         String[] userProperties = fixedGame.split(FIXED_GAME_FIELD_SEPARATOR);
         userProperties = checkAndRepairFields(userProperties);
         return generateGameFromProperties(userProperties);
     }
 
     private GameDTO generateGameFromProperties(String[] userProperties) throws TestGameProcessorException
     {
         GameDTO gameDTO = new GameDTO();
         gameDTO.setTitle(getString(userProperties, Field.GAME_TITLE));
         gameDTO.setPremiereDate(getDate(userProperties, Field.GAME_PREMIERE_DATE));
         gameDTO.setPegiRating(getPegiEnum(getString(userProperties, Field.GAME_PEGI_ENUM)));
         gameDTO.setGameTypes(getOrGenerateGameTypesList(getStringList(userProperties, Field.GAME_TYPES)));
         gameDTO.setPlatforms(getOrGeneratePlatformsList(getStringList(userProperties, Field.GAME_PLATFORMS)));
         gameDTO.setProducers(getOrGenerateCompaniesList(getStringList(userProperties, Field.GAME_PRODUCERS)));
         gameDTO.setPublishers(getOrGenerateCompaniesList(getStringList(userProperties, Field.GAME_PUBLISHERS)));
         try
         {
             gameDTO.setPoints(Integer.valueOf(getString(userProperties, Field.GAME_POINTS)));
         }
         catch (NumberFormatException ex)
         {
            GeneralLogger.warn(this, "Number format exception while parsing points field: " + Arrays.toString(userProperties) + ", defaulting point to 0");
             gameDTO.setPoints(0);
         }
         gameDTO.setGameStatus(getGameStatusEnum(getString(userProperties, Field.GAME_STATUS)));
         gameDTO.setAddedDate(getDate(userProperties, Field.GAME_ADDED_DATE_TIME));
         GeneralLogger.info(this, "Generated game: " + gameDTO.toString());
         return gameDTO;
     }
 
     private String[] checkAndRepairFields(String[] userProperties) throws TestGameProcessorException
     {
         if (userProperties == null || userProperties.length == 0
                 || userProperties.length != Field.values().length)
         {
             throw new TestGameProcessorException("fixed.user.property: " + Arrays.toString(userProperties) + " has wrong format, omitting.");
         }
 
         for (Field field : Field.values())
         {
             userProperties = checkAndRepair(userProperties, field);
         }
         return userProperties;
     }
 
     private String[] checkAndRepair(String[] userProperties, Field field) throws TestGameProcessorException
     {
         String property = getString(userProperties, field);
         FieldType fieldType1 = field.getFieldType();
         switch (fieldType1)
         {
             case STRING_ARRAY:
                 userProperties[field.getIndexPosition()] = fixStringArray(property);
                 break;
             case DATE:
                 userProperties[field.getIndexPosition()] = fixDate(property);
                 break;
             case DATETIME:
                 userProperties[field.getIndexPosition()] = fixDateTime(property);
                 break;
             default:
                 userProperties[field.getIndexPosition()] = fixString(property);
                 break;
         }
         return userProperties;
     }
 
     private Set<CompanyDTO> getOrGenerateCompaniesList(List<String> companiesNames) throws TestGameProcessorException
     {
         Set<CompanyDTO> companiesList = new HashSet<CompanyDTO>();
         for (String companyName : companiesNames)
         {
             companiesList.add(getOrGenerateCompany(companyName));
         }
         return companiesList;
     }
 
     private CompanyDTO getOrGenerateCompany(String companyName) throws TestGameProcessorException
     {
         CompanyDTO companyByName =
                 companyDAO.getCompanyByName(companyName);
         if (companyByName == null)
         {
             GeneralLogger.warn(this, "Company not found, generating new from name [" + companyName + "]");
             CompanyDTO companyDTO = new CompanyDTO(companyName);
             companyDAO.saveOrUpdateCompany(companyDTO);
             companyByName = companyDAO.getCompanyByName(companyName);
         }
         return companyByName;
     }
 
     private Set<GameTypeDTO> getOrGenerateGameTypesList(List<String> gameTypesNames)
     {
         Set<GameTypeDTO> gameTypeDTOList = new HashSet<GameTypeDTO>();
         for (String gameTypeName : gameTypesNames)
         {
             gameTypeDTOList.add(getOrGenerateGameType(gameTypeName));
         }
         return gameTypeDTOList;
     }
 
     private GameTypeDTO getOrGenerateGameType(String gameTypeName)
     {
         GameTypeEnum gameTypeEnum;
         try
         {
             gameTypeEnum = GameTypeEnum.valueOf(gameTypeName);
         }
         catch (IllegalArgumentException ex)
         {
             gameTypeEnum = GameTypeEnum.UNKNOWN;
         }
         return gameTypeDAO.getGameType(gameTypeEnum.getId());
     }
 
     private Set<PlatformDTO> getOrGeneratePlatformsList(List<String> platformNames)
     {
         Set<PlatformDTO> platformDTOs = new HashSet<PlatformDTO>();
         for (String platformName : platformNames)
         {
             platformDTOs.add(getOrGeneratePlatform(platformName));
         }
         return platformDTOs;
     }
 
     private PlatformDTO getOrGeneratePlatform(String platformName)
     {
         PlatformEnum platformEnum;
         try
         {
             platformEnum = PlatformEnum.valueOf(platformName);
         }
         catch (IllegalArgumentException ex)
         {
             platformEnum = PlatformEnum.UNKNOWN;
         }
         return platformDAO.getPlatform(platformEnum.getId());
     }
 
     private PegiEnum getPegiEnum(String enumValue)
     {
         try
         {
             return PegiEnum.valueOf(enumValue);
         }
         catch (IllegalArgumentException ex)
         {
             return PegiEnum.UNKNOWN;
         }
     }
 
     private GameStatusEnum getGameStatusEnum(String enumValue)
     {
         try
         {
             return GameStatusEnum.valueOf(enumValue);
         }
         catch (IllegalArgumentException ex)
         {
             return GameStatusEnum.UNKNOWN;
         }
     }
 
     private String fixString(String string)
     {
         if (string == null)
         {
             return "";
         }
         return string.trim();
     }
 
     private String fixStringArray(String string)
     {
         if (string == null)
         {
             return fixString(string);
         }
         String[] splitted = string.split(FIXED_GAME_ARRAY_SEPARATOR);
         if (splitted == null)
         {
             return fixString(string);
         }
         StringBuilder sb = new StringBuilder();
         for (String split : splitted)
         {
             sb.append(split.trim()).append(FIXED_GAME_ARRAY_SEPARATOR);
         }
         for (int i = 0; i < FIXED_GAME_ARRAY_SEPARATOR.length(); i++)
         {
             sb.deleteCharAt(sb.length() - 1);
         }
         return sb.toString();
     }
 
     private String fixDate(String string)
     {
         try
         {
             return DateConverter.getDateString(DateConverter.getDate(string));
         }
         catch (ParseException e)
         {
             GeneralLogger.warn(this, "Parse exception when fixing date [" + string + "], defaulting it to current system time.");
         }
         return DateConverter.getDateString(new Date(System.currentTimeMillis()));
     }
 
     private String fixDateTime(String string)
     {
         try
         {
             return DateConverter.getDateTimeString(DateConverter.getDate(string));
         }
         catch (ParseException e)
         {
             GeneralLogger.warn(this, "Parse exception when fixing datetime [" + string + "], defaulting it to current system time.");
         }
         return DateConverter.getDateTimeString(new Date(System.currentTimeMillis()));
     }
 
     //PROPERTIES_FIELD_ENUM(arrayIndex,FieldType)
     public enum Field
     {
         GAME_TITLE(0, STRING),
         GAME_PREMIERE_DATE(1, DATE),
         GAME_PEGI_ENUM(2, STRING),
         GAME_TYPES(3, STRING_ARRAY),
         GAME_PLATFORMS(4, STRING_ARRAY),
         GAME_PRODUCERS(5, STRING_ARRAY),
         GAME_PUBLISHERS(6, STRING_ARRAY),
         GAME_POINTS(7, STRING),
         GAME_STATUS(8, STRING),
         GAME_ADDED_DATE_TIME(9, DATETIME);
 
         private int indexPosition;
         private FieldType fieldType;
 
         Field(int newIndexPosition, FieldType fieldType)
         {
             this.indexPosition = newIndexPosition;
             this.fieldType = fieldType;
         }
 
         public int getIndexPosition()
         {
             return this.indexPosition;
         }
 
         public FieldType getFieldType()
         {
             return fieldType;
         }
     }
 
     public enum FieldType
     {
         STRING,
         STRING_ARRAY,
         DATE,
         DATETIME
     }
 
     private Date getDate(String[] userProperties, Field fieldType) throws TestGameProcessorException
     {
         if (fieldType.getFieldType() != DATE)
         {
             throw new TestGameProcessorException("GetDate - wrong function used to get field of type " + fieldType.name());
         }
         String dateInString = getString(userProperties, fieldType);
         try
         {
             return DateConverter.getDate(dateInString);
         }
         catch (ParseException e)
         {
             throw new TestGameProcessorException("Date parse exception for property: " + dateInString);
         }
     }
 
     private String getString(String[] userProperties, Field fieldType) throws TestGameProcessorException
     {
         return getStringList(userProperties, fieldType).get(0);
     }
 
     private List<String> getStringList(String[] userProperties, Field fieldType) throws TestGameProcessorException
     {
         if (userProperties.length < fieldType.getIndexPosition())
         {
             throw new TestGameProcessorException("Wrong property index - ArrayOutOfBounds exception for " + Arrays.toString(userProperties) + " for fieldType " + fieldType.name());
         }
 
         return getSplittedList(userProperties[fieldType.getIndexPosition()]);
     }
 
     private List<String> getSplittedList(String property) throws TestGameProcessorException
     {
         String[] propertiesArray = property.split(",");
         List<String> resultProperties = new LinkedList<String>();
         if (propertiesArray == null)
         {
             throw new TestGameProcessorException("Property is null after array split: " + property);
         }
         else
         {
             for (String resultProperty : propertiesArray)
             {
                 String trimmedProperty = resultProperty.trim();
                 if (!resultProperties.contains(trimmedProperty)
                         && !trimmedProperty.isEmpty())
                 {
                     resultProperties.add(trimmedProperty);
                 }
             }
         }
         return resultProperties;
     }
 }
