 package ru.yaal.project.hhapi.dictionary.entry.entries.vacancy;
 
 import ru.yaal.project.hhapi.dictionary.entry.AbstractDictionaryEntry;
 
 public class VacancySearchFields extends AbstractDictionaryEntry {
    public static final VacancySearchFields NULL_VACANCY_SEARCH_FIELD = new VacancySearchFields();
    public static final VacancySearchFields VACANCY_NAME = new VacancySearchFields("name", "  ");
    public static final VacancySearchFields COMPANY_NAME = new VacancySearchFields("company_name", "  ");
    public static final VacancySearchFields DESCRIPTION = new VacancySearchFields("description", "  ");

    public VacancySearchFields() {
    }

    public VacancySearchFields(String id, String name) {
        super(id, name);
    }
 
 }
