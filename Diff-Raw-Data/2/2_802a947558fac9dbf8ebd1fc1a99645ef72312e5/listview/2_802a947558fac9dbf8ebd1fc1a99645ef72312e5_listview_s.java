         
         ListView listview_{{ string_array }} = (ListView) findViewById(R.id.list_{{ string_array }});
         String[] items = getResources().getStringArray(R.array.list_{{string_array}});
         ArrayAdapter<String> adapter_{{string_array}} = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
         listview_{{string_array}}.setAdapter(adapter_{{string_array}});
 
         listview_{{ string_array }}.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {
                 Intent i = null;
                 switch(position){
                     {% for child in children %}
                 case {{ child["position"] }}:
                     i = new Intent(getBaseContext(), {{ child["class_name"] }}.class);
                     break;
                     {% endfor %}
                 default:
                     break;
                 }
                // EditText Caca
                 {%- for etext_id in etext_list %}
                 etext_{{ etext_id }} = (EditText) findViewById(R.id.etext_{{etext_id}});
                 report.set_{{ etext_id }}(etext_{{ etext_id }}.getText().toString());
                 {%- endfor %}
                 startActivity(i);			
 			}
 		});
