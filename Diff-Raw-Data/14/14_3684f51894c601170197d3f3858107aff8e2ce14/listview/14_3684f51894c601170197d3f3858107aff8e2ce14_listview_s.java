 
        ListView listview_{{string_array}} = (ListView) findViewById(R.id.{{ string_array }});
        String[] items = getResources().getStringArray(R.array.{{string_array}});
         ArrayAdapter<String> adapter_{{string_array}} = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
         listview_{{string_array}}.setAdapter(adapter_{{string_array}});
