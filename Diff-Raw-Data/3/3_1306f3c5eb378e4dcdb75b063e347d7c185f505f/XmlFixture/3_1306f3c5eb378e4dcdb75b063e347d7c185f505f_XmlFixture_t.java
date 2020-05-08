 package fixture;
 
 public class XmlFixture {
 
     public static String getExpectedBeneficiaryCaseXml() {
 
         return "<?xml version=\"1.0\"?>\n" +
                 "<data xmlns=\"http://openrosa.org/formdesigner/A6E4F029-A971-41F1-80C1-9DDD5CC24571\" uiVersion=\"1\" version=\"12\"\n" +
                 "      name=\"Registration\">\n" +
                 "    <beneficiary_information>\n" +
                 "        <beneficiary_name>test-case</beneficiary_name>\n" +
                 "        <beneficiary_code>XYZ/123</beneficiary_code>\n" +
                 "        <beneficiary_dob>12-12-1988</beneficiary_dob>\n" +
                 "        <receiving_organization>XAQ</receiving_organization>\n" +
                 "        <sex>male</sex>\n" +
                 "        <title>MR</title>\n" +
                 "    </beneficiary_information>\n" +
                 "    <caregiver_information>\n" +
                 "        <caregiver_code>cg1</caregiver_code>\n" +
                 "        <caregiver_name>cg1</caregiver_name>\n" +
                 "    </caregiver_information>\n" +
                 "    <form_type>beneficiary_registration</form_type>\n" +
                 "    <case>\n" +
                 "        <case_id>c7264b49-4e3d-4659-8df3-7316539829cb</case_id>\n" +
                 "        <date_modified>2012-05-02T22:18:45.071+05:30</date_modified>\n" +
                 "        <create>\n" +
                 "            <case_type_id>beneficiary</case_type_id>\n" +
                 "            <case_name>XYZ/123</case_name>\n" +
                 "            <user_id>f98589102c60fcc2e0f3c422bb361ebd</user_id>\n" +
                 "            <external_id>XYZ/123</external_id>\n" +
                 "        </create>\n" +
                 "        <update>\n" +
                 "            <beneficiary_dob>12-12-1988</beneficiary_dob>\n" +
                 "            <title>MR</title>\n" +
                 "            <beneficiary_name>test-case</beneficiary_name>\n" +
                 "            <receiving_organization>XAQ</receiving_organization>\n" +
                 "            <caregiver_name>cg1</caregiver_name>\n" +
                 "            <sex>male</sex>\n" +
                 "            <form_type>beneficiary_registration</form_type>\n" +
                 "            <beneficiary_code>XYZ/123</beneficiary_code>\n" +
                 "            <caregiver_code>cg1</caregiver_code>\n" +
                 "        </update>\n" +
                 "    </case>\n" +
                 "    <meta>\n" +
                 "        <username>cg1</username>\n" +
                 "        <userID>f98589102c60fcc2e0f3c422bb361ebd</userID>\n" +
                 "    </meta>\n" +
                 "</data>";
     }
 
     public static String getExpectedUpdateOwnerXml() {
         return "<?xml version='1.0' ?>\n" +
                 "<data uiVersion=\"1\" version=\"89\" name=\"update\"\n" +
                 "      xmlns=\"http://openrosa.org/formdesigner/E45F3EFD-A7BE-42A6-97C2-5133E766A2AA\">\n" +
                 "    <owner_id>hw1</owner_id>\n" +
                 "    <form_type>custom_update</form_type>\n" +
                 "    <case>\n" +
                 "        <case_id>c7264b49-4e3d-4659-8df3-7316539829cb</case_id>\n" +
                 "        <date_modified>2012-05-02T22:18:45.071+05:30</date_modified>\n" +
                 "        <update>\n" +
                 "            <form_type>custom_update</form_type>\n" +
                 "            <owner_id>hw1</owner_id>\n" +
                 "        </update>\n" +
                 "    </case>\n" +
                 "    <meta>\n" +
                 "        <username>cg1</username>\n" +
                 "        <userID>f98589102c60fcc2e0f3c422bb361ebd</userID>\n" +
                 "    </meta>\n" +
                 "</data>";
     }
 
     public static String getExpectedFacilityXml() {
         return "<Registration xmlns=\"http://openrosa.org/user/registration\">\n" +
                 "    <username>someFacilityCode</username>\n" +
                 "    <password>1234</password>\n" +
                 "    <uuid>FAC1</uuid>\n" +
                 "    <date></date>\n" +
                 "    <registering_phone_id>1234567890</registering_phone_id>\n" +
                 /*"    <user_data>\n" +
                 "        <data key=\"district\">someFacilityDistrict</data>\n" +
                 "        <data key=\"village\">someFacilityVillage</data>\n" +
                 "        <data key=\"ward\">someFacilityWard</data>\n" +
                 "        <data key=\"constituency\">someFacilityConstituency</data>\n" +
                 "        <data key=\"optionalPhoneNumber1\">987654321</data>\n" +
                 "        <data key=\"optionalPhoneNumber2\">23142345645</data>\n" +
                 "        <data key=\"optionalPhoneNumber3\">253767569</data>\n" +
                 "    </user_data>\n" +*/
                 "</Registration>";
     }
 
 
     public static String getExpectedUserFormXml() {
         return "<Registration xmlns=\"http://openrosa.org/user/registration\">\n" +
                 "    <username>uName</username>\n" +
                 "    <password>1234</password>\n" +
                 "    <uuid>id</uuid>\n" +
                 "    <date>01-01-2012</date>\n" +
                 "    <registering_phone_id>11111</registering_phone_id>\n" +
                 "    <user_data>\n" +
                 "        <data key=\"caregiverCode\">code</data>\n" +
                 "        <data key=\"firstName\">fName</data>\n" +
                 "        <data key=\"middleName\">mName</data>\n" +
                 "        <data key=\"lastName\">lName</data>\n" +
                 "        <data key=\"gender\">gender</data>\n" +
                 "    </user_data>\n" +
                 "</Registration>";
     }
 
     public static String getExpectedFacilityCaseXml() {
         return "<?xml version='1.0' ?>\n" +
                "<data uiVersion=\"1\" version=\"40\" name=\"Facility Registration\"\n" +
                "      xmlns=\"http://openrosa.org/formdesigner/8F9255C2-3A1D-4B82-8346-561264CEBEA0\">\n" +
                 "    <facility_code>someFacilityCode</facility_code>\n" +
                 "    <facility_name>FAC01-Name</facility_name>\n" +
                 "    <case>\n" +
                 "        <case_id>FAC1</case_id>\n" +
                 "        <create>\n" +
                 "            <case_type_id>facility</case_type_id>\n" +
                 "            <case_name>someFacilityCode</case_name>\n" +
                 "            <user_id>FAC1</user_id>\n" +
                 "            <external_id>someFacilityCode</external_id>\n" +
                 "        </create>\n" +
                 "        <update>\n" +
                 "            <facility_name>FAC01-Name</facility_name>\n" +
                 "            <facility_code>someFacilityCode</facility_code>\n" +
                 "        </update>\n" +
                 "    </case>\n" +
                 "    <meta>\n" +
                 "        <username>allUsers</username>\n" +
                 "        <userID>FAC1</userID>\n" +
                 "    </meta>\n" +
                 "</data>\n" +
                 "\n";
     }
 }
